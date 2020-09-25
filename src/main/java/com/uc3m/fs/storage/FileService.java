package com.uc3m.fs.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FileService {

	private FileRepository fileRepository;

	@Autowired
	public FileService(FileRepository fileRepository) {
		this.fileRepository = fileRepository;
	}

	public List<File> listAll() {
		List<File> files = new ArrayList<>();
		fileRepository.findAll().forEach(files::add);
		return files;
	}

	public File findByUuid(String uuid) {
		return fileRepository.findById(uuid).orElse(null);
	}

	public File save(File file, String[] sites) {
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < sites.length; i++) {
			s.append(FileRepository.SITE_OPEN);
			s.append(sites[i]);
			s.append(FileRepository.SITE_CLOSE);
			if (i!=sites.length-1) s.append(FileRepository.SITE_SEPARATOR);
		}
		file.setSites(s.toString());
		fileRepository.save(file);
		return file;
	}

	public void delete(String uuid) {
		fileRepository.deleteById(uuid);
	}

	public List<File> findByOwner(String owner) {
		Optional<List<File>> o = fileRepository.findByOwner(owner);
		if (o.isPresent()) return o.get();
		return new ArrayList<>(0);
	}

	public List<File> findBySite(String site) {
		Optional<List<File>> o = fileRepository.findBySite(site);
		if (o.isPresent()) return o.get();
		return new ArrayList<>(0);
	}

}