package com.uc3m.fs.storage;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uc3m.fs.storage.exceptions.FileServiceException;
import com.uc3m.fs.storage.model.DeploymentRequest;
import com.uc3m.fs.storage.model.DeploymentRequestPK;
import com.uc3m.fs.storage.model.File;
import com.uc3m.fs.storage.model.FilePK;
import com.uc3m.fs.storage.model.Site;

import javassist.NotFoundException;

@Service
public class FileService {

	private final String STATUS_DEPLOY = "FOR_DEPLOY", STATUS_DEPLOYED = "DEPLOYED";

	private FileRepository fileRepository;
	private DeploymentRequestRepository deploymentRequestRepository;

	@Autowired
	public FileService(FileRepository fileRepository, DeploymentRequestRepository deploymentRequestRepository) {
		this.fileRepository = fileRepository;
		this.deploymentRequestRepository = deploymentRequestRepository;
	}

	public List<File> listAll() {
		List<File> files = new ArrayList<>();
		fileRepository.findAll().forEach(files::add);
		return files;
	}

	public File findById(String uuid, String owner) {
		return fileRepository.findById(new FilePK(uuid, owner)).orElse(null);
	}

	@Transactional
	public File save(File file, String[] sites) throws FileServiceException {
		try {
			fileRepository.save(file);
			ArrayList<DeploymentRequest> requests = new ArrayList<>(sites.length);
			if (sites.length > 0) {
				for (int i = 0; i < sites.length; i++) {
					requests.add(new DeploymentRequest(
							new DeploymentRequestPK(file.getUuid(), file.getOwner(), sites[i]),
							file, null, STATUS_DEPLOY));
				}
			}
			deploymentRequestRepository.saveAll(requests);
		} catch (Exception e) {
			throw new FileServiceException(e.getMessage());
		}
		return file;
	}

	@Transactional
	public void deploy(String uuid, String owner, String site) throws Exception {
		File f = findById(uuid, owner);
		if (f == null) throw new FileNotFoundException(uuid + " file not found");

		boolean updated = false;
		List<DeploymentRequest> dr = f.getDeploymentRequests();
		for (int i = 0; i < dr.size() && !updated; i++) {
			if (dr.get(i).getSite().equals(site)) {
				if (dr.get(i).getStatus().equals(STATUS_DEPLOYED))
					throw new NotFoundException(uuid + " with site " + site + " already deployed");
				dr.get(i).setStatus(STATUS_DEPLOYED);
				updated = true;
				break;
			}
		}
		if (!updated) throw new NotFoundException(uuid + " file not found");
	}

	public void delete(String uuid, String owner) {
		fileRepository.deleteById(new FilePK(uuid, owner));
	}
	/**
	 * Delete deployment request. If is the last request remove file
	 * @return true if was the last request
	 */
	public boolean delete(File file, String site) throws NotFoundException {
		List<DeploymentRequest> dr = file.getDeploymentRequests();
		for (DeploymentRequest r: dr) {
			if (r.getSite().equals(site)) {
				if (dr.size()==1) {
					fileRepository.delete(file);
					return true;
				} else {
					deploymentRequestRepository.delete(r);
					return false;
				}
			}
		}
		throw new NotFoundException("There is no deployment request with " + site);
	}

	public List<File> findByOwner(String owner) {
		return fileRepository.findByOwner(owner);
	}

	public List<DeploymentRequest> findBySite(String site) {
		return deploymentRequestRepository.findBySiteBean(new Site(site));
	}

	@Transactional
	public List<DeploymentRequest> findBySites(String[] sites) {
		List<DeploymentRequest> deploymentRequest = new ArrayList<>();
		// For every site we'll find managed files
		for (String s : sites)
			deploymentRequest.addAll(findBySite(s));

		return deploymentRequest;
	}

}