package com.uc3m.fs;

import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.validation.ConstraintViolationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
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

import com.uc3m.fs.storage.StorageService;
import com.uc3m.fs.storage.exceptions.StorageFileNotFoundException;

@RestController
public class FS_Controller {

	private final StorageService storageService;

	@Autowired
	public FS_Controller(StorageService storageService) {
		this.storageService = storageService;
	}

	/**
		https://spring.io/guides/gs/accessing-data-jpa/
el usuario con rol SiteManager podrá listar y descargar aquellos ficheros (VMs)
que tengan como site asociado el suyo, puede listar aquellas VMs que tengan
que ser desplegadas en su site para saber los sites que administra un usuario existe funcionalidad en el RBAC
SecurityConfig.ROLE_SITE_MANAGER
SecurityConfig.ROLE_USER
	 */

	@GetMapping(value = Config.PATH_DOWNLOAD + "/{fileUuid}")
	public ResponseEntity<String> download(@PathVariable(value = "fileUuid", required = true) String uuid) {
		try {
			// TODO verify authority (SiteManager with his site and user author)
			InputStream is = storageService.loadAsResource(uuid).getInputStream();
			byte[] file = StreamUtils.copyToByteArray(is);
			is.close();
			String b64 = Base64.getEncoder().encodeToString(file);
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
			@RequestParam(name = "List<site>", required = false) String[] sites) {
		try {
			Thread.sleep(2000);
			// TODO add to database
			if (uuid == null || uuid.isEmpty())
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

			if (sites != null) {
				for (int i = 0; i < sites.length; i++)
					if (sites[i].isEmpty())
						return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}

			storageService.store(file, uuid);
			return new ResponseEntity<>(HttpStatus.ACCEPTED);
		} catch (FileAlreadyExistsException e) {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping(value = Config.PATH_LIST_FOR_USER)
	public ResponseEntity<List<String>> list_for_user() {// TODO
		try {
			// TODO If SiteManager all his sites; If user only owned
			List<String> l=new ArrayList<String>();
			l.add("nombre");
			l.add("uuidPostman");
			l.add("cccccccccccccccccccccccc cccccccccccccccccccccccc cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc");
			l.add("cccccc");
			l.add("cccccc");
			l.add("cccccc");
			l.add("cccccc");
			l.add("cccccc");
			l.add("cccccc");
			l.add("cccccc");
			l.add("cccccc");
			l.add("cccccc");
			l.add("cccccc");
			l.add("cccccc");
			l.add("cccccc");
			l.add("cccccc");
			l.add("cccccc");
			l.add("cccccc");
			l.add("cccccc");
			l.add("cccccc");
			return new ResponseEntity<>(l, HttpStatus.OK);
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