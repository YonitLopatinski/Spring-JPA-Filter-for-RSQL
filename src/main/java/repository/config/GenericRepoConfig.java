package repository.config;

import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import repository.GenericRepositoryImpl;

@Configuration
@EnableJpaRepositories(basePackages = "repository",
        includeFilters = {@ComponentScan.Filter(type = FilterType.REGEX, pattern = "*.repository.*")},
        repositoryBaseClass = GenericRepositoryImpl.class)
public class GenericRepoConfig {
}
