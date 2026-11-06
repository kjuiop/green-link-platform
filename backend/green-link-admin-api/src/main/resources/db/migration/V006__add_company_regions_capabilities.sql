CREATE TABLE company_regions (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    company_id BIGINT      NOT NULL,
    sido       VARCHAR(50) NOT NULL,
    sigungu    VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_company_region (company_id, sido, sigungu),
    CONSTRAINT fk_region_company FOREIGN KEY (company_id) REFERENCES companies (id)
);

CREATE TABLE company_capabilities (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    company_id BIGINT      NOT NULL,
    category   VARCHAR(30) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_company_capability (company_id, category),
    CONSTRAINT fk_capability_company FOREIGN KEY (company_id) REFERENCES companies (id)
);
