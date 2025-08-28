package co.com.pragma.api.proposal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "routes.paths")
public class ProposalPath {
    private String proposals;
}
