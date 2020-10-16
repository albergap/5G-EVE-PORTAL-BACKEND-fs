package com.uc3m.fs.storage;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import com.uc3m.fs.Config;
import com.uc3m.fs.storage.exceptions.StorageException;
import com.uc3m.fs.storage.exceptions.StorageFileNotFoundException;

@Service
public class StorageService {

	private final Path rootLocation;

	@Autowired
	public StorageService() {
		this.rootLocation = Paths.get(Config.FILES_DIR_LOCATION);
	}

	private static File createUserFolder(String email) throws Exception {
		try {
			if (email.contains("\\")) throw new Exception();
			File f = new File(Config.FILES_DIR_LOCATION + File.separator + email);
			if (!f.exists()) f.mkdir();
			return f;
		} catch (Exception e1) {
			throw new Exception("Failed to create user folder.");
		}
	}

	@SuppressWarnings("unused")
	private static void removeUserFolder(String email) throws Exception {// TODO delete functionality
		if (email.contains("\\")) throw new Exception();
		File f = new File(email);
		if (f.exists() && f.isDirectory() && f.listFiles().length==0)
			f.delete();
	}

	public Resource readFile(String uuid, String email) throws IOException {
		return loadAsResource(email + File.separator + uuid);
	}

	public void store(MultipartFile file, String uuid, String email) throws StorageException, FileAlreadyExistsException {
		try {
			if (file.isEmpty()) throw new StorageException("Failed to store empty file " + file.getOriginalFilename());

			createUserFolder(email);
			Path path = rootLocation.resolve(email + File.separator + uuid);
			if (Files.exists(path)) throw new FileAlreadyExistsException("");

			file.transferTo(new File(path.toUri()));
		} catch (FileAlreadyExistsException e) {
			throw new FileAlreadyExistsException("File " + uuid + " already exists");
		} catch (Exception e) {
			throw new StorageException("Failed to store file: " + uuid, e);
		}
	}

	public Stream<Path> loadAll() {
		try {
			return Files.walk(this.rootLocation, 1)
					.filter(path -> !path.equals(this.rootLocation))
					.map(path -> this.rootLocation.relativize(path));
		} catch (IOException e) {
			throw new StorageException("Failed to read stored files", e);
		}
	}

	public Path load(String filename) {
		return rootLocation.resolve(filename);
	}

	public Resource loadAsResource(String filename) {
		try {
			Path file = load(filename);
			Resource resource = new UrlResource(file.toUri());
			if(resource.exists() || resource.isReadable()) {
				return resource;
			} else {
				throw new StorageFileNotFoundException("File not found: " + filename);
			}
		} catch (MalformedURLException e) {
			throw new StorageFileNotFoundException("Could not read file: " + filename, e);
		}
	}

	public void deleteAll() {
		FileSystemUtils.deleteRecursively(rootLocation.toFile());
	}

	public void init() {
		try {
			Files.createDirectory(rootLocation);
		} catch (FileAlreadyExistsException e) {
		} catch (IOException e) {
			throw new StorageException("Could not initialize storage", e);
		}
	}

}