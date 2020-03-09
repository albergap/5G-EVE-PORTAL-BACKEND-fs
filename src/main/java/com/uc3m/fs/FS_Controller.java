package com.uc3m.fs;

import java.util.Base64;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import com.uc3m.fs.model.FileUploadRequest;
import com.uc3m.fs.storage.StorageService;
import com.uc3m.fs.storage.exceptions.StorageFileNotFoundException;

@RestController
public class FS_Controller {
	// Upload a MultipartFile (without chunks):
	// https://spring.io/guides/gs/uploading-files
	// Upload a file in chunks:
	// https://stackoverflow.com/questions/26964688/multipart-file-upload-using-spring-rest-template-spring-web-mvc

	private final StorageService storageService;

	@Autowired
	public FS_Controller(StorageService storageService) {
		this.storageService = storageService;
	}

	@GetMapping(value = Config.PATH + "download/{fileUuid}")
	public ResponseEntity<String> download(@NotBlank @PathVariable(value = "fileUuid", required = true) String uuid) {
		// TODO: FORBIDDEN, CONFLICT
		try {
			String b64 = Base64.getEncoder()
					.encodeToString(StreamUtils.copyToByteArray(storageService.loadAsResource(uuid).getInputStream()));
			return new ResponseEntity<>(b64, HttpStatus.OK);
		} catch (StorageFileNotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping(value = Config.PATH + "upload")
	public ResponseEntity<Void> upload(@RequestParam("file") MultipartFile file, @Valid @RequestBody(required = true) FileUploadRequest f) {
		// TODO: FORBIDDEN, NOT FOUND, CONFLICT, INTERNAL ERROR
		try {
			System.out.println("FileUploaded: " + f);
			storageService.store(file);
			return new ResponseEntity<>(HttpStatus.ACCEPTED);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<?> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException exc) {
		return ResponseEntity.badRequest().build();
	}

	@ExceptionHandler(StorageFileNotFoundException.class)
	public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
		return ResponseEntity.badRequest().build();
	}

}