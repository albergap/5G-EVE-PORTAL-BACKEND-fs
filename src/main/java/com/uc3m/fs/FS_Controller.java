package com.uc3m.fs;

import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import com.uc3m.fs.keycloak.KeycloakUtil;
import com.uc3m.fs.rbac.RBACRestService;
import com.uc3m.fs.storage.File;
import com.uc3m.fs.storage.FileId;
import com.uc3m.fs.storage.FileService;
import com.uc3m.fs.storage.StorageService;
import com.uc3m.fs.storage.exceptions.StorageFileNotFoundException;

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

	@GetMapping(value = Config.PATH_DOWNLOAD + "/{fileUuid}")
	public ResponseEntity<String> download(@PathVariable(value = "fileUuid", required = true) String uuid, HttpServletRequest request) {
		try {
			String userId = KeycloakUtil.getIdUser(request);
			File file = fileService.findById(uuid, userId);
			if (file == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

			// Keycloak
			boolean accessByRole = false;
			boolean userRole = KeycloakUtil.isUserRole(request), managerRole = KeycloakUtil.isManagerRole(request);

			// Verify ownership
			if (userRole && userId.equals(file.getOwner())) accessByRole = true;
			// Verify manager permission
			if (managerRole) {
				StringTokenizer tok = new StringTokenizer(file.getSites(), ",");
				String[] sitesFile = new String[tok.countTokens()];
				for (int i = 0; i < sitesFile.length; i++) {
					String s = (String) tok.nextElement();
					sitesFile[i] = s.substring(1, s.length()-1);
				}

				String[] sitesUser = RBACRestService.getSitesOfUser(request.getHeader(HttpHeaders.AUTHORIZATION));

				for (int i = 0; i < sitesFile.length && !accessByRole; i++) {
					// If a site is managed -> access
					for (int j = 0; j < sitesUser.length && !accessByRole; j++)
						if (sitesFile[i].equals(sitesUser[j])) accessByRole = true;
				}
			}

			if (!accessByRole) return new ResponseEntity<>(HttpStatus.FORBIDDEN);

			// Read file and convert to B64
			String b64 = Base64.getEncoder().encodeToString(storageService.readFile(uuid));
			return new ResponseEntity<>(b64, HttpStatus.OK);
		} catch (StorageFileNotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
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
			@RequestParam(name = "List<site>", required = false) String[] sites,
			HttpServletRequest request) {
		String idUser = null;
		try {
			// Params validation
			if (uuid == null || uuid.isEmpty())
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

			if (sites != null) {
				for (int i = 0; i < sites.length; i++)
					if (sites[i].isEmpty())
						return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}

			// Save file to DB
			idUser = KeycloakUtil.getIdUser(request);
			File f = new File(new FileId(uuid, idUser), "");
			fileService.save(f, sites);

			// Save file to persistence
			storageService.store(file, uuid);

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

	@GetMapping(value = Config.PATH_LIST_FOR_USER)
	public ResponseEntity<List<String>> list_for_user(HttpServletRequest request) {
		try {
			List<File> files = new ArrayList<File>();
			// Keycloak
			String userId = KeycloakUtil.getIdUser(request);
			boolean userRole = KeycloakUtil.isUserRole(request), managerRole = KeycloakUtil.isManagerRole(request);

			// Add files owner
			if (userRole) {
				files.addAll(fileService.findByOwner(userId));
			}
			// Add files of managed sites
			if (managerRole) {
				String[] sites = RBACRestService.getSitesOfUser(request.getHeader(HttpHeaders.AUTHORIZATION));
				// Add all files with his sites
				files.addAll(fileService.findBySites(sites));
			}

			// Return result list
			if (files.size() == 0) return new ResponseEntity<>(HttpStatus.NO_CONTENT);

			List<String> result = new ArrayList<String>(files.size());
			for (File f : files) result.add(f.getUuid());
			return new ResponseEntity<>(result, HttpStatus.OK);
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