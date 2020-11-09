package com.uc3m.fs.storage.db;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uc3m.fs.exceptions.DBException;
import com.uc3m.fs.exceptions.DBFileNotFoundException;
import com.uc3m.fs.storage.db.entities.DeploymentRequest;
import com.uc3m.fs.storage.db.entities.DeploymentRequestPK;
import com.uc3m.fs.storage.db.entities.File;
import com.uc3m.fs.storage.db.entities.FilePK;
import com.uc3m.fs.storage.db.entities.Site;

@Service
public class FileService {

	private static final String STATUS_DEPLOY = "FOR_DEPLOY", STATUS_DEPLOYED = "DEPLOYED";

	private FileRepository fileRepository;
	private DeploymentRequestRepository deploymentRequestRepository;

	@Autowired
	public FileService(FileRepository fileRepository, DeploymentRequestRepository deploymentRequestRepository) {
		this.fileRepository = fileRepository;
		this.deploymentRequestRepository = deploymentRequestRepository;
	}

	/*public List<File> getAllFiles() {
		List<File> files = new ArrayList<>();
		fileRepository.findAll().forEach(files::add);
		return files;
	}*/

	public File findFilesById(String uuid, String owner) {
		return fileRepository.findById(new FilePK(uuid, owner)).orElse(null);
	}

	/**
	 * Insert file and deployment requests
	 */
	@Transactional
	public File saveFile(File file, String[] sites) throws DBException {
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
			return file;
		} catch (Exception e) {
			throw new DBException(e.getMessage(), e);
		}
	}

	@Transactional
	public void deployDeploymentRequest(String uuid, String owner, String site) throws DBFileNotFoundException, Exception {
		File f = findFilesById(uuid, owner);
		if (f == null) throw new DBFileNotFoundException(uuid + " file not found");

		boolean updated = false;
		List<DeploymentRequest> dr = f.getDeploymentRequests();
		for (int i = 0; i < dr.size() && !updated; i++) {
			if (dr.get(i).getSite().equals(site)) {
				if (dr.get(i).getStatus().equals(STATUS_DEPLOYED))
					throw new DBException(uuid + " with site " + site + " already deployed");
				dr.get(i).setStatus(STATUS_DEPLOYED);
				updated = true;
				break;
			}
		}
		if (!updated) throw new DBFileNotFoundException(uuid + " file not found");
	}

	public void deleteFileById(String uuid, String owner) {
		fileRepository.deleteById(new FilePK(uuid, owner));
	}
	/**
	 * Delete deployment request. If is the last request -> remove file
	 * @return {@link Boolean} if it was the last request
	 */
	public boolean deleteDeploymentRequest(File file, String site) throws DBFileNotFoundException {
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
		throw new DBFileNotFoundException("There is no deployment request with " + site);
	}

	public List<File> findFilesByOwner(String owner) {
		return fileRepository.findByOwner(owner);
	}

	public List<DeploymentRequest> findDeploymentRequestsBySite(String site) {
		return deploymentRequestRepository.findBySiteBean(new Site(site));
	}

	@Transactional
	public List<DeploymentRequest> findDeploymentRequestsBySites(String[] sites) {
		List<DeploymentRequest> deploymentRequest = new ArrayList<>();
		// For every site we'll find managed files
		for (String s : sites)
			deploymentRequest.addAll(findDeploymentRequestsBySite(s));

		return deploymentRequest;
	}

}