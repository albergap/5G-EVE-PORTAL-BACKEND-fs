package com.uc3m.fs.storage;

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

	public void store(MultipartFile file, String name) throws StorageException, FileAlreadyExistsException {
		try {
			if (file.isEmpty()) throw new StorageException("Failed to store empty file " + file.getOriginalFilename());

			Path path = rootLocation.resolve(name);
			if (Files.exists(path)) throw new FileAlreadyExistsException("");

			Files.copy(file.getInputStream(), path);
		} catch (FileAlreadyExistsException e) {
			throw new FileAlreadyExistsException("File " + name + " already exists");
		} catch (Exception e) {
			throw new StorageException("Failed to store file " + file.getOriginalFilename(), e);
		}
	}

	public void store(MultipartFile file) throws StorageException, FileAlreadyExistsException {
		store(file, file.getOriginalFilename());
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
			}
			else {
				throw new StorageFileNotFoundException("Could not read file: " + filename);

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