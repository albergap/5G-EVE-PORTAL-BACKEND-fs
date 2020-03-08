package com.uc3m.fs;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Base64;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.uc3m.fs.model.FileUploadRequest;
import com.uc3m.fs.storage.StorageService;
import com.uc3m.fs.storage.exceptions.StorageFileNotFoundException;

@RestController
public class FS_Controller {
	// https://spring.io/guides/gs/uploading-files

	private final StorageService storageService;

	@Autowired
	public FS_Controller(StorageService storageService) {
		this.storageService = storageService;
	}

	@GetMapping(value = Config.URL + "download/{fileUuid}")
	public ResponseEntity<String> download(@NotBlank @PathVariable(value = "fileUuid", required = true) String uuid) {
		// TO-DO: FORBIDDEN, CONFLICT
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

	@PostMapping(value = Config.URL + "upload")
	public ResponseEntity<Void> upload(@Valid @RequestBody(required = true) FileUploadRequest f) {
		// TO-DO: FORBIDDEN, NOT FOUND, CONFLICT, INTERNAL ERROR
		try {
			System.out.println("FileUploaded: " + f);
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