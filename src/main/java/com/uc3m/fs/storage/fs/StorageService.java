package com.uc3m.fs.storage.fs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.uc3m.fs.Config;
import com.uc3m.fs.exceptions.FSException;
import com.uc3m.fs.exceptions.FSFileAlreadyExistsException;
import com.uc3m.fs.exceptions.FSFileNotFoundException;

@Service
public class StorageService {

	private final Path rootLocation;

	@Autowired
	public StorageService() {
		this.rootLocation = Paths.get(Config.FILES_DIR_LOCATION);
	}

	/**
	 * Create the user folder if doesnt exists
	 */
	private static File createUserFolder(String email) throws FSException {
		try {
			if (email.contains("\\")) throw new Exception();
			File f = new File(Config.FILES_DIR_LOCATION + File.separator + email);
			if (!f.exists()) f.mkdir();
			return f;
		} catch (Exception e) {
			throw new FSException("Failed to create user folder.", e);
		}
	}

	/**
	 * Remove the user folder if is empty
	 */
	private static void removeUserFolder(String email) throws FSException {
		if (email.contains("\\")) throw new FSException("Character not allowed");
		try {
			File f = new File(email);
			if (f.exists() && f.isDirectory() && f.listFiles().length==0)
				f.delete();
		} catch (Exception e) {
			throw new FSException(e.getMessage(), e);
		}
	}

	/**
	 * Remove file and user folder
	 */
	public boolean removeFile(String email, String uuid) throws FSFileNotFoundException, FSException {
		try {
			Resource file = readFile(uuid, email);
			boolean resul = file.getFile().delete();
			removeUserFolder(email);
			return resul;
		} catch (FileNotFoundException e) {
			throw new FSFileNotFoundException(e.getMessage(), e);
		} catch (IOException e) {
			throw new FSException(e.getMessage(), e);
		}
	}

	public Resource readFile(String uuid, String email) throws FSException {
		return loadAsResource(email + File.separator + uuid);
	}

	/**
	 * Save file and create user folder
	 */
	public void store(MultipartFile file, String uuid, String email) throws FSFileAlreadyExistsException, FSException {
		try {
			if (file.isEmpty()) throw new FSException("Failed to store empty file " + file.getOriginalFilename());

			createUserFolder(email);
			Path path = rootLocation.resolve(email + File.separator + uuid);
			if (Files.exists(path)) throw new FileAlreadyExistsException("");

			file.transferTo(new File(path.toUri()));
		} catch (FileAlreadyExistsException e) {
			throw new FSFileAlreadyExistsException("File " + uuid + " already exists");
		} catch (Exception e) {
			throw new FSException("Failed to store file: " + uuid, e);
		}
	}

	/*private Stream<Path> loadAll() {
		try {
			return Files.walk(this.rootLocation, 1)
					.filter(path -> !path.equals(this.rootLocation))
					.map(path -> this.rootLocation.relativize(path));
		} catch (IOException e) {
			throw new StorageException("Failed to read stored files", e);
		}
	}*/

	private Resource loadAsResource(String filename) throws FSException {
		try {
			Path file = rootLocation.resolve(filename);
			Resource resource = new UrlResource(file.toUri());
			if(resource.exists() || resource.isReadable()) {
				return resource;
			} else {
				throw new FSFileNotFoundException("File not found: " + filename);
			}
		} catch (MalformedURLException e) {
			throw new FSFileNotFoundException("Could not read file: " + filename, e);
		}
	}

	/*public void deleteAll() {
		FileSystemUtils.deleteRecursively(rootLocation.toFile());
	}*/

	public void init() throws FSException {
		try {
			Files.createDirectory(rootLocation);
		} catch (FileAlreadyExistsException e) {
		} catch (IOException e) {
			throw new FSException("Could not initialize storage", e);
		}
	}

}