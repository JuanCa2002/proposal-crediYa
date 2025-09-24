package co.com.pragma.config;

import co.com.pragma.model.proposal.gateways.ProposalRepository;
import co.com.pragma.model.proposaltype.gateways.ProposalTypeRepository;
import co.com.pragma.model.restconsumer.gateways.LambdaInsertProposal;
import co.com.pragma.model.restconsumer.gateways.LambdaLoanPlan;
import co.com.pragma.model.sqs.gateways.SQSProposalNotification;
import co.com.pragma.model.state.gateways.StateRepository;
import co.com.pragma.model.user.gateways.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UseCasesConfigTest {

    @Test
    void testUseCaseBeansExist() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class)) {
            String[] beanNames = context.getBeanDefinitionNames();

            boolean useCaseBeanFound = false;
            for (String beanName : beanNames) {
                if (beanName.endsWith("UseCase")) {
                    useCaseBeanFound = true;
                    break;
                }
            }

            assertTrue(useCaseBeanFound, "No beans ending with 'Use Case' were found");
        }
    }

    @Configuration
    @Import(UseCasesConfig.class)
    static class TestConfig {

        @Bean
        public LambdaInsertProposal lambdaInsertProposal() {
            return org.mockito.Mockito.mock(LambdaInsertProposal.class);
        }

        @Bean
        public ProposalRepository proposalRepository() {
            return org.mockito.Mockito.mock(ProposalRepository.class);
        }

        @Bean
        public ProposalTypeRepository proposalTypeRepository() {
            return org.mockito.Mockito.mock(ProposalTypeRepository.class);
        }

        @Bean
        public StateRepository stateRepository() {
            return org.mockito.Mockito.mock(StateRepository.class);
        }

        @Bean
        public UserRepository userRepository() {
            return org.mockito.Mockito.mock(UserRepository.class);
        }

        @Bean
        public SQSProposalNotification sQSProposalNotification() {
            return org.mockito.Mockito.mock(SQSProposalNotification.class);
        }

        @Bean
        public LambdaLoanPlan lambdaLoanPlan() {
            return org.mockito.Mockito.mock(LambdaLoanPlan.class);
        }

        @Bean
        public MyUseCase myUseCase() {
            return new MyUseCase();
        }
    }

    static class MyUseCase {
        public String execute() {
            return "MyUseCase Test";
        }
    }
}