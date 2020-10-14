package com.uc3m.fs.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uc3m.fs.model.FileResponse;

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

	public List<FileResponse> findBySites(String[] sites) {
		List<FileResponse> files = new ArrayList<>();
		// For every site we'll find managed files
		for (String s : sites) {
			List<File> filesFind = findBySite(s);
			for (int i = 0; i < filesFind.size(); i++) {
				boolean added = false;
				// Search for multiple sites -> add to sites list
				for (FileResponse f : files) {
					if (f.uuid.equals(filesFind.get(i).getUuid()) && f.owner.equals(filesFind.get(i).getOwner())) {
						f.sites.add(s);
						added = true;
					}
				}
				// If first encounter -> create the file response
				if (!added) {
					ArrayList<String> managedSites=new ArrayList<String>();
					managedSites.add(s);
					files.add(new FileResponse(filesFind.get(i).getUuid(), filesFind.get(i).getOwner(), managedSites));
				}
			}
		}

		return files;
	}

	public static ArrayList<String> getSites(String sites) {
		if (sites==null || sites.length()==0) return new ArrayList<>();

		StringTokenizer tok = new StringTokenizer(sites, SITE_SEPARATOR);
		ArrayList<String> result = new ArrayList<String>(tok.countTokens());
		while (tok.hasMoreElements()) {
			String site = (String) tok.nextElement();
			result.add(site.substring(1, site.length()-1));
		}
		return result;
	}

}