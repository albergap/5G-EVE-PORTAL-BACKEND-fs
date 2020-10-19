package com.uc3m.fs.storage;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.uc3m.fs.storage.model.DeploymentRequest;
import com.uc3m.fs.storage.model.DeploymentRequestPK;
import com.uc3m.fs.storage.model.Site;

public interface DeploymentRequestRepository extends CrudRepository<DeploymentRequest, DeploymentRequestPK> {

	List<DeploymentRequest> findBySiteBean(Site siteBean);

}