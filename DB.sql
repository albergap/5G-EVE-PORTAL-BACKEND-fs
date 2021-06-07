DROP TABLE IF EXISTS public.files;
CREATE TABLE public.files (
	uuid VARCHAR NOT NULL,
	owner VARCHAR NOT NULL,
	PRIMARY KEY (uuid,owner)
);

INSERT INTO public.sites VALUES
	('FRANCE_NICE'),
	('FRANCE_PARIS'),
	('FRANCE_RENNES'),
	('GREECE_ATHENS'),
	('ITALY_TURIN'),
	('SPAIN_5TONIC');


DROP TABLE IF EXISTS public.sites;
CREATE TABLE public.sites (
	site VARCHAR NOT NULL,
	PRIMARY KEY (site)
);


DROP TABLE IF EXISTS public.deployment_requests;
CREATE TABLE public.deployment_requests (
	uuid VARCHAR NOT NULL,
	owner VARCHAR NOT NULL,
	site VARCHAR NOT NULL,,
	date_request timestamp(3) NOT NULL,
	status VARCHAR NOT NULL,
	PRIMARY KEY (uuid,owner,site),
	CONSTRAINT fk_files FOREIGN KEY(uuid,owner) REFERENCES files(uuid,owner) ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT fk_sites FOREIGN KEY(site) REFERENCES sites(site) ON DELETE CASCADE ON UPDATE CASCADE
);