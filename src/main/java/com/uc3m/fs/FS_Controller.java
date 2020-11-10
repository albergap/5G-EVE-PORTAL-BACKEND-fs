package com.uc3m.fs;

import java.io.FileInputStream;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import com.uc3m.fs.exceptions.DBException;
import com.uc3m.fs.exceptions.DBFileNotFoundException;
import com.uc3m.fs.exceptions.FSFileAlreadyExistsException;
import com.uc3m.fs.exceptions.FSFileNotFoundException;
import com.uc3m.fs.keycloak.RequestProperties;
import com.uc3m.fs.model.DeploymentRequestResponse;
import com.uc3m.fs.model.FileResponse;
import com.uc3m.fs.rbac.RBACRestService;
import com.uc3m.fs.storage.db.FileService;
import com.uc3m.fs.storage.db.entities.DeploymentRequest;
import com.uc3m.fs.storage.db.entities.File;
import com.uc3m.fs.storage.fs.StorageService;

@RestController
public class FS_Controller {

	private static final String PARENT_PATH = "fs", PATH_ID_PARAMETERS = "{uuid}/{owner}", PATH_DEPLOYMET_REQUEST = PARENT_PATH + "/deployment_request";

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

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException e) {
		return ResponseEntity.badRequest().build();
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<?> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
		return ResponseEntity.badRequest().build();
	}

	// -------------------- Auxiliar functions --------------------

	private static boolean isFileOwnership(RequestProperties request, File file) {
		return request.developerRole && request.getUserId().equals(file.getId().getOwner());
	}
	private static boolean isFileManaged(String bearerToken, RequestProperties request, File file) throws Exception {
		if (request.managerRole) {
			// Get all sites of file, auxiliar variable
			String[] sitesFile = new String[file.getDeploymentRequests().size()];
			for (int i = 0; i < sitesFile.length; i++)
				sitesFile[i] = file.getDeploymentRequests().get(i).getSite();

			// Check if is managed
			String[] sitesUser = RBACRestService.getSitesOfUser(bearerToken);
			for (int i = 0; i < sitesFile.length; i++) {
				// If a site is managed -> access
				for (int j = 0; j < sitesUser.length; j++)
					if (sitesFile[i].equals(sitesUser[j])) return true;
			}
		}
		return false;
	}
	private static boolean verifyAuthorizedFileAccess(HttpServletRequest request, RequestProperties requestProperties, File file) throws Exception {
		return isFileOwnership(requestProperties, file) || isFileManaged(request.getHeader(HttpHeaders.AUTHORIZATION), requestProperties, file);
	}

	// -------------------- File methods functions --------------------

	/**
	 * Create FileResponses for every file
	 * EXAMPLE: 2 DeploymentRequest (different sites) in the same file will result 1 FileResponse with 2 sites in the list
	 */
	private static List<FileResponse> getFilesBySites(List<DeploymentRequest> deploymentRequest) {
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

	@GetMapping(value = PARENT_PATH + "/" + PATH_ID_PARAMETERS)
	public ResponseEntity<FileResponse> getInfoFile(
			@PathVariable(required = true) String uuid,
			@PathVariable(required = true) String owner,
			HttpServletRequest request) {
		try {
			RequestProperties rProp = new RequestProperties(request);
			if (!rProp.authenticated) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			// Allowed both roles

			// Exists in DB
			File file = fileService.findFilesById(uuid, owner);
			if (file == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

			// Verify access
			if (!verifyAuthorizedFileAccess(request, rProp, file)) return new ResponseEntity<>(HttpStatus.FORBIDDEN);

			return new ResponseEntity<FileResponse>(new FileResponse(file), HttpStatus.OK);
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

	@GetMapping(value = PARENT_PATH + "/download/" + PATH_ID_PARAMETERS, produces="application/zip")
	public ResponseEntity<InputStreamResource> download(@PathVariable(required = true) String uuid, @PathVariable(required = true) String owner,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			RequestProperties rProp = new RequestProperties(request);
			if (!rProp.authenticated) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			// Allowed both roles

			// Exists in DB
			File file = fileService.findFilesById(uuid, owner);
			if (file == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

			// Verify access
			if (!verifyAuthorizedFileAccess(request, rProp, file)) return new ResponseEntity<>(HttpStatus.FORBIDDEN);

			// Read file
			Resource fileRead = storageService.readFile(uuid, file.getId().getOwner());
			// Response
			response.setHeader("Content-Disposition", "attachment;filename=" + file.getId().getUuid());
			response.setContentLengthLong(fileRead.getFile().length());
			return ResponseEntity.ok().body(new InputStreamResource(new FileInputStream(fileRead.getFile())));
		} catch (FSFileNotFoundException e) {
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

	@PostMapping(value = PARENT_PATH, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseBody
	public ResponseEntity<Void> upload(// Form params
			@RequestPart(name = "file", required = true) MultipartFile file,
			@RequestParam(name = "uuid", required = true) String uuid,
			@RequestParam(name = "sites", required = true) String[] sites,
			HttpServletRequest request) {
		String idDeveloper = null;
		try {
			RequestProperties rProp = new RequestProperties(request);
			if (!rProp.authenticated) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			// Allowed ROLE_DEVELOPER
			if (!rProp.developerRole) return new ResponseEntity<>(HttpStatus.FORBIDDEN);

			// Params validation
			if (uuid.isEmpty()) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

			for (int i = 0; i < sites.length; i++)
				if (sites[i].isEmpty()) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

			// Save file to DB
			idDeveloper = rProp.getUserId();
			File f = new File(uuid, idDeveloper);
			fileService.saveFile(f, sites);

			// Save file to persistence
			storageService.store(file, uuid, idDeveloper);

			return new ResponseEntity<>(HttpStatus.ACCEPTED);
		} catch (FSFileAlreadyExistsException e) {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		} catch (Exception e) {
			try {
				fileService.deleteFileById(uuid, idDeveloper);
			} catch (Exception e1) {
				e1.printStackTrace();
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping(value = PARENT_PATH)
	public ResponseEntity<List<FileResponse>> listFiles(HttpServletRequest request) {
		try {
			RequestProperties rProp = new RequestProperties(request);
			if (!rProp.authenticated) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			// Allowed both roles

			List<FileResponse> result = null;

			// Add files owned
			if (rProp.developerRole) {
				// Get file
				List<File> files = fileService.findFilesByOwner(rProp.getUserId());
				if (files.size() == 0) return new ResponseEntity<>(HttpStatus.NO_CONTENT);

				// Convert to array of FileResponse
				result = new ArrayList<FileResponse>(files.size());
				FileResponse fr;
				for (int i = 0; i < files.size(); i++) {
					fr = new FileResponse(
							files.get(i).getUuid(),
							files.get(i).getOwner(),
							new ArrayList<DeploymentRequestResponse>(files.get(i).getDeploymentRequests().size())
							);
					// Deployment requests
					for (int j = 0; j < files.get(i).getDeploymentRequests().size(); j++)
						fr.deploymentRequests.add(new DeploymentRequestResponse(
								files.get(i).getDeploymentRequests().get(j).getSite(), files.get(i).getDeploymentRequests().get(j).getStatus()
								));
					result.add(fr);
				}
			}
			// Add files of managed sites
			if (rProp.managerRole) {
				String[] sites = RBACRestService.getSitesOfUser(request.getHeader(HttpHeaders.AUTHORIZATION));
				// Add all files with his sites
				result = getFilesBySites(fileService.findDeploymentRequestsBySites(sites));
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

	@DeleteMapping(value = PARENT_PATH + "/" + PATH_ID_PARAMETERS)
	public ResponseEntity<?> deleteFile(@PathVariable(required = true) String uuid, @PathVariable(required = true) String owner, HttpServletRequest request) {
		try {
			RequestProperties rProp = new RequestProperties(request);
			if (!rProp.authenticated) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			// Allowed ROLE_DEVELOPER
			if (!rProp.developerRole) return new ResponseEntity<>(HttpStatus.FORBIDDEN);

			// Get file
			File file = fileService.findFilesById(uuid, owner);
			if (file == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

			// Delete file
			fileService.deleteFileById(uuid, owner);
			if (storageService.removeFile(file.getOwner(), file.getUuid()))
				return new ResponseEntity<>(HttpStatus.OK);
			else
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// -------------------- Deployment requests methods functions --------------------

	@GetMapping(value = PATH_DEPLOYMET_REQUEST + "/" + PATH_ID_PARAMETERS)
	public ResponseEntity<List<DeploymentRequestResponse>> getDeploymentRequests(// TODO
			@PathVariable(required = true) String uuid,
			@PathVariable(required = true) String owner,
			@RequestParam(required = false) String site,
			HttpServletRequest request) {
		try {
			RequestProperties rProp = new RequestProperties(request);
			if (!rProp.authenticated) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			// Allowed both roles

			// Get file
			File file = fileService.findFilesById(uuid, owner);
			if (file == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

			// Verify access
			if (!verifyAuthorizedFileAccess(request, rProp, file)) return new ResponseEntity<>(HttpStatus.FORBIDDEN);

			// Get deployment requests
			List<DeploymentRequest> dr = file.getDeploymentRequests();
			if (dr.isEmpty()) return new ResponseEntity<>(HttpStatus.NO_CONTENT);

			// Result
			List<DeploymentRequestResponse> result = new ArrayList<DeploymentRequestResponse>(dr.size());
			for (DeploymentRequest d : dr)
				result.add(new DeploymentRequestResponse(d.getSite(), d.getStatus()));

			return new ResponseEntity<List<DeploymentRequestResponse>>(result, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping(value = PATH_DEPLOYMET_REQUEST + "/" + PATH_ID_PARAMETERS)
	public ResponseEntity<?> addDeploymentRequests(// TODO
			@PathVariable(required = true) String uuid,
			@PathVariable(required = true) String owner,
			@RequestBody(required = true) String[] sites,
			HttpServletRequest request) {
		try {
			RequestProperties rProp = new RequestProperties(request);
			if (!rProp.authenticated) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			// Allowed ROLE_DEVELOPER
			if (!rProp.developerRole) return new ResponseEntity<>(HttpStatus.FORBIDDEN);

			// Params validation
			if (sites.length == 0) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			for (int i = 0; i < sites.length; i++)
				if (sites[i].isEmpty()) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

			// Get file
			File file = fileService.findFilesById(uuid, owner);
			if (file == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

			fileService.insertDeploymentRequests(file, sites);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping(value = PATH_DEPLOYMET_REQUEST + "/deploy/" + PATH_ID_PARAMETERS)
	public ResponseEntity<?> deployDeploymentRequest(
			@PathVariable(required = true) String uuid,
			@PathVariable(required = true) String owner,
			@RequestParam(required = true) String site,
			HttpServletRequest request) {
		try {
			RequestProperties rProp = new RequestProperties(request);
			if (!rProp.authenticated) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			// Allowed ROLE_MANAGER
			if (!rProp.managerRole) return new ResponseEntity<>(HttpStatus.FORBIDDEN);

			// Params validation
			if (site.isEmpty()) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

			// Deploy
			fileService.deployDeploymentRequest(uuid, owner, site);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (DBFileNotFoundException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
		} catch (DBException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping(value = PATH_DEPLOYMET_REQUEST + "/" + PATH_ID_PARAMETERS)
	public ResponseEntity<?> deleteDeploymentRequest(@PathVariable(required = true) String uuid,
			@PathVariable(required = true) String owner,
			@RequestParam(required = true) String site,
			HttpServletRequest request) {
		try {
			RequestProperties rProp = new RequestProperties(request);
			if (!rProp.authenticated) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			// Allowed ROLE_DEVELOPER
			if (!rProp.developerRole) return new ResponseEntity<>(HttpStatus.FORBIDDEN);

			// Params validation
			if (site.equals("")) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

			// Find file
			File file = fileService.findFilesById(uuid, owner);
			if (file == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

			// Delete deployment request
			boolean lastDeploymentRequest = fileService.deleteDeploymentRequest(file, site);
			if (lastDeploymentRequest) {
				// Remove file
				if (storageService.removeFile(file.getOwner(), file.getUuid()))
					return new ResponseEntity<>(HttpStatus.OK);
				else
					return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			} else {
				return new ResponseEntity<>(HttpStatus.OK);
			}
		} catch (DBFileNotFoundException | FSFileNotFoundException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}