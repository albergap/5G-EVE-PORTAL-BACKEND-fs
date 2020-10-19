-- Version 1
DROP TABLE IF EXISTS public.files;

CREATE TABLE public.files (
	uuid VARCHAR,
	owner VARCHAR,
	sites VARCHAR,
	PRIMARY KEY (uuid, owner)
);

INSERT INTO public.files (uuid, "owner", sites) VALUES ('uuidPostman3.zip', 'user1@mail.com', '(s1)');
INSERT INTO public.files (uuid, "owner", sites) VALUES ('uuidPostman2.zip', 'user1@mail.com', '(s3)');
INSERT INTO public.files (uuid, "owner", sites) VALUES ('uuidPostman1.zip', 'user2@mail.com', '');
INSERT INTO public.files (uuid, "owner", sites) VALUES ('uuidPostman4.zip', 'user1@mail.com', '(s1)');
INSERT INTO public.files (uuid, "owner", sites) VALUES ('uuidPostman5.zip', 'user1@mail.com', '');
INSERT INTO public.files (uuid, "owner", sites) VALUES ('uuidPostman1.zip', 'user1@mail.com', '(s1)');
INSERT INTO public.files (uuid, "owner", sites) VALUES ('das', 'user1@mail.com', '(FRANCE_NICE),(ITALY_TURIN)');



-- Version 2
DROP TABLE IF EXISTS public.files;
CREATE TABLE public.files (
	uuid VARCHAR NOT NULL,
	owner VARCHAR NOT NULL,
	PRIMARY KEY (uuid,owner)
);

DROP TABLE IF EXISTS public.sites;
CREATE TABLE public.sites (
	site VARCHAR NOT NULL,
	PRIMARY KEY (site)
);

DROP TABLE IF EXISTS public.deployment_requests;
CREATE TABLE public.deployment_requests (
	uuid VARCHAR NOT NULL,
	owner VARCHAR NOT NULL,
	site VARCHAR NOT NULL,
	status VARCHAR NOT NULL,
	PRIMARY KEY (uuid,owner,site),
	CONSTRAINT fk_files FOREIGN KEY(uuid,owner) REFERENCES files(uuid,owner) ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT fk_sites FOREIGN KEY(site) REFERENCES sites(site) ON DELETE CASCADE ON UPDATE CASCADE
);


-- Tests
INSERT INTO public.sites VALUES
	('FRANCE_NICE'),
	('FRANCE_PARIS'),
	('FRANCE_RENNES'),
	('GREECE_ATHENS'),
	('ITALY_TURIN'),
	('SPAIN_5TONIC');
INSERT INTO public.files VALUES
	('uuidPostman1.zip', 'user1@mail.com'),
	('uuidPostman2.zip', 'user1@mail.com'),
	('uuidPostman3.zip', 'user1@mail.com'),
	('uuidPostman1.zip', 'user2@mail.com');

INSERT INTO public.deployment_requests VALUES
	('uuidPostman1.zip', 'user1@mail.com', 'SPAIN_5TONIC', 'DEPLOY'),
	('uuidPostman1.zip', 'user1@mail.com', 'ITALY_TURIN', 'DEPLOY');
INSERT INTO public.deployment_requests VALUES ('uuidPostman2.zip', 'user1@mail.com', 'SPAIN_5TONIC', 'DEPLOY');
INSERT INTO public.deployment_requests VALUES ('uuidPostman3.zip', 'user1@mail.com', 'ITALY_TURIN', 'DEPLOY');
INSERT INTO public.deployment_requests VALUES ('uuidPostman1.zip', 'user2@mail.com', 'FRANCE_NICE', 'DEPLOY');