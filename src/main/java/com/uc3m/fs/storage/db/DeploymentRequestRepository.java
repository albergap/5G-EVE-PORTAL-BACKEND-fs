package com.uc3m.fs.storage.db;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.uc3m.fs.storage.db.entities.DeploymentRequest;
import com.uc3m.fs.storage.db.entities.DeploymentRequestPK;
import com.uc3m.fs.storage.db.entities.Site;

interface DeploymentRequestRepository extends CrudRepository<DeploymentRequest, DeploymentRequestPK> {

	List<DeploymentRequest> findBySiteBean(Site siteBean);

}