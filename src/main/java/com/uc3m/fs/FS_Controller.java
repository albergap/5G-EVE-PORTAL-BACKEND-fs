package com.uc3m.fs;

import com.uc3m.fs.model.FileUploaded;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FS_Controller {

	@GetMapping(value = "portal/fs/download/{fileUuid}")
	public ResponseEntity<String> download(@PathVariable(value = "fileUuid", required = true) String uuid) {
		// BAD REQUEST, FORBIDDEN, NOT FOUND, CONFLICT, INTERNAL ERROR
		try {
			System.out.println("uuid = " + uuid);
			return new ResponseEntity<>("Base64", HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping(value = "portal/fs/upload")
	public ResponseEntity<Void> upload(@RequestBody(required = true) FileUploaded f) {
		// BAD REQUEST, FORBIDDEN, NOT FOUND, CONFLICT, INTERNAL ERROR
		try {
			System.out.println("FileUploaded: " + f);
			return new ResponseEntity<>(HttpStatus.ACCEPTED);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}