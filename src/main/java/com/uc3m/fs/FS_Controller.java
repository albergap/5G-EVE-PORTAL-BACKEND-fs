package com.uc3m.fs;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import com.uc3m.fs.keycloak.KeycloakUtil;
import com.uc3m.fs.model.DeploymentRequestResponse;
import com.uc3m.fs.model.FileResponse;
import com.uc3m.fs.rbac.RBACRestService;
import com.uc3m.fs.storage.FileService;
import com.uc3m.fs.storage.StorageService;
import com.uc3m.fs.storage.exceptions.StorageFileNotFoundException;
import com.uc3m.fs.storage.model.DeploymentRequest;
import com.uc3m.fs.storage.model.File;

import javassist.NotFoundException;

@RestController
public class FS_Controller {

	@Autowired
	private FileService fileService;
	private final StorageService storageService;

	@Autowired
	public FS_Controller(StorageService storageService) {
		this.storageService = storageService;
	}
	@Autowired
	public void setProductService(FileService fileService) {
		this.fileService = fileService;
	}

	private static boolean ownership(HttpServletRequest request, File file) {
		boolean userRole = KeycloakUtil.isUserRole(request);
		if (!userRole) return false;
		String userId = KeycloakUtil.getIdUser(request);
		return (userRole && userId.equals(file.getId().getOwner()));
	}
	private static boolean managed(HttpServletRequest request, File file) throws Exception {
		if (!KeycloakUtil.isManagerRole(request)) return false;

		// Get all sites of file, auxiliar variable
		String[] sitesFile = new String[file.getDeploymentRequests().size()];
		for (int i = 0; i < sitesFile.length; i++)
			sitesFile[i] = file.getDeploymentRequests().get(i).getSite();

		// Check if is managed
		String[] sitesUser = RBACRestService.getSitesOfUser(request.getHeader(HttpHeaders.AUTHORIZATION));
		for (int i = 0; i < sitesFile.length; i++) {
			// If a site is managed -> access
			for (int j = 0; j < sitesUser.length; j++)
				if (sitesFile[i].equals(sitesUser[j])) {
					return true;
				}
		}
		return false;
	}

	private static boolean authorizedAccessFile(HttpServletRequest request, File file) throws Exception {
		if (ownership(request, file)) return true;
		if (managed(request, file)) return true;
		return false;
	}

	public static List<FileResponse> getFilesBySites(List<DeploymentRequest> deploymentRequest) {
		// Create FileResponses for every file
		// Example: 2 DeploymentRequest with the same file and different sites
		// will result 1 FileResponse with 2 sites in the list

		List<FileResponse> result = new ArrayList<FileResponse>(deploymentRequest.size());
		for (int i = 0; i < deploymentRequest.size(); i++) {
			boolean added = false;
			// Search for multiple sites -> add to sites list
			for (FileResponse fr : result) {
				if (fr.uuid.equals(deploymentRequest.get(i).getId().getUuid()) &&
						fr.owner.equals(deploymentRequest.get(i).getId().getOwner())) {
					fr.deploymentRequests.add(
							new DeploymentRequestResponse(deploymentRequest.get(i).getSite(), deploymentRequest.get(i).getStatus())
							);
					added = true;
				}
			}
			// If first encounter -> create the file response
			if (!added) {
				ArrayList<DeploymentRequestResponse> deploymentRequestResponse = new ArrayList<DeploymentRequestResponse>();
				deploymentRequestResponse.add(
						new DeploymentRequestResponse(deploymentRequest.get(i).getSite(), deploymentRequest.get(i).getStatus())
						);
				result.add(new FileResponse(
						deploymentRequest.get(i).getId().getUuid(),
						deploymentRequest.get(i).getId().getOwner(),
						deploymentRequestResponse)
						);
			}
		}
		return result;
	}

	@GetMapping(value = Config.PATH_DOWNLOAD + "/{fileUuid}/{owner}", produces="application/zip")
	public ResponseEntity<InputStreamResource> download(@PathVariable(value = "fileUuid", required = true) String uuid, @PathVariable(required = true) String owner,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			// Exists in DB
			File file = fileService.findById(uuid, owner);
			if (file == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

			// Verify access
			if (!authorizedAccessFile(request, file)) return new ResponseEntity<>(HttpStatus.FORBIDDEN);

			// Read file
			Resource fileRead = storageService.readFile(uuid, file.getId().getOwner());
			// Response
			response.setHeader("Content-Disposition", "attachment;filename=" + file.getId().getUuid());
			response.setContentLengthLong(fileRead.getFile().length());
			return ResponseEntity.ok().body(new InputStreamResource(new FileInputStream(fileRead.getFile())));
		} catch (StorageFileNotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} catch (HttpClientErrorException e) {
			if (e.getStatusCode()==HttpStatus.UNAUTHORIZED)
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping(value = Config.PATH_UPLOAD, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseBody
	public ResponseEntity<Void> upload(
			@RequestPart(name = "file", required = true) MultipartFile file,
			@RequestParam(name = "dzuuid", required = true) String uuid,
			@RequestParam(name = "List<site>", required = true) String[] sites,
			HttpServletRequest request) {
		String idUser = null;
		try {
			// Params validation
			if (uuid.isEmpty()) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

			for (int i = 0; i < sites.length; i++)
				if (sites[i].isEmpty()) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

			// Save file to DB
			idUser = KeycloakUtil.getIdUser(request);
			File f = new File(uuid, idUser);
			fileService.save(f, sites);

			// Save file to persistence
			storageService.store(file, uuid, idUser);

			return new ResponseEntity<>(HttpStatus.ACCEPTED);
		} catch (FileAlreadyExistsException e) {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		} catch (Exception e) {
			try {
				fileService.delete(uuid, idUser);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping(value = Config.PATH_LIST)
	public ResponseEntity<List<FileResponse>> list(HttpServletRequest request) {
		try {
			List<FileResponse> result = null;

			// Keycloak
			String userId = KeycloakUtil.getIdUser(request);
			boolean userRole = KeycloakUtil.isUserRole(request), managerRole = KeycloakUtil.isManagerRole(request);

			// Add files owner
			if (userRole) {
				// Get from DB
				List<File> files = fileService.findByOwner(userId);
				if (files.size() == 0) return new ResponseEntity<>(HttpStatus.NO_CONTENT);

				// Convert to array of String
				result = new ArrayList<FileResponse>(files.size());
				FileResponse fr;
				for (int i = 0; i < files.size(); i++) {
					fr = new FileResponse(
							files.get(i).getUuid(),
							files.get(i).getOwner(),
							new ArrayList<DeploymentRequestResponse>(files.get(i).getDeploymentRequests().size())
							);
					for (int j = 0; j < files.get(i).getDeploymentRequests().size(); j++)
						fr.deploymentRequests.add(new DeploymentRequestResponse(
								files.get(i).getDeploymentRequests().get(j).getSite(), files.get(i).getDeploymentRequests().get(j).getStatus()
								));
					result.add(fr);
				}
			}
			// Add files of managed sites
			if (managerRole) {
				String[] sites = RBACRestService.getSitesOfUser(request.getHeader(HttpHeaders.AUTHORIZATION));
				// Add all files with his sites
				result = getFilesBySites(fileService.findBySites(sites));
				if (result.size() == 0) return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<>(result, HttpStatus.OK);
		} catch (HttpClientErrorException e) {
			if (e.getStatusCode()==HttpStatus.UNAUTHORIZED)
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping(value = Config.PATH_DEPLOY + "/{fileUuid}/{owner}")
	public ResponseEntity<?> deploy(
			@PathVariable(value = "fileUuid", required = true) String uuid,
			@PathVariable(required = true) String owner,
			@RequestParam(required = true) String site,
			HttpServletRequest request) {
		try {
			if (uuid.isEmpty() || owner.isEmpty() || site.isEmpty()) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

			fileService.deploy(uuid, owner, site);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (FileNotFoundException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
		} catch (NotFoundException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping(value = Config.PATH_DELETE + "/{fileUuid}/{owner}")
	public ResponseEntity<?> delete(@PathVariable(value = "fileUuid", required = true) String uuid, @PathVariable(required = true) String owner) {
		try {
			if (uuid.isEmpty() || owner.isEmpty()) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

			File file = fileService.findById(uuid, owner);
			if (file == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

			fileService.delete(uuid, owner);
			if (storageService.removeFile(file.getOwner(), file.getUuid()))
				return new ResponseEntity<>(HttpStatus.OK);
			else
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException e) {
		return ResponseEntity.badRequest().build();
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<?> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
		return ResponseEntity.badRequest().build();
	}

}