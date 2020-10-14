package com.uc3m.fs.storage;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FileService {

	private static final String SITE_SEPARATOR = ",", SITE_OPEN = "(", SITE_CLOSE = ")";
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

	public File findById(String uuid, String owner) {
		return fileRepository.findById(new FileId(uuid, owner)).orElse(null);
	}

	public File save(File file, String[] sites) {
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < sites.length; i++) {
			s.append(SITE_OPEN);
			s.append(sites[i]);
			s.append(SITE_CLOSE);
			if (i!=sites.length-1) s.append(SITE_SEPARATOR);
		}
		file.setSites(s.toString());
		fileRepository.save(file);
		return file;
	}

	public void delete(String uuid, String owner) {
		fileRepository.deleteById(new FileId(uuid, owner));
	}

	public List<File> findByOwner(String owner) {
		Optional<List<File>> o = fileRepository.findByOwner(owner);
		if (o.isPresent()) return o.get();
		return new ArrayList<>(0);
	}

	public List<File> findBySite(String site) {
		Optional<List<File>> o = fileRepository.findBySitesContaining(SITE_OPEN + site + SITE_CLOSE);
		if (o.isPresent()) return o.get();
		return new ArrayList<>(0);
	}

	public Set<File> findBySites(String[] sites) {
		Set<File> files = new LinkedHashSet<>();
		for (String s : sites)
			files.addAll(findBySite(s));

		return files;
	}

}