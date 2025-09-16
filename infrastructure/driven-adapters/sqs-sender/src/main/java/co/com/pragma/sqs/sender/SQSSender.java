package co.com.pragma.sqs.sender;

import co.com.pragma.model.loan.Loan;
import co.com.pragma.model.proposal.Proposal;
import co.com.pragma.model.sqs.gateways.SQSProposalNotification;
import co.com.pragma.sqs.sender.config.SQSSenderProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.Locale;

@Service
@Log4j2
@RequiredArgsConstructor
public class SQSSender implements SQSProposalNotification {
    private final SQSSenderProperties properties;
    private final SqsAsyncClient client;

    public Mono<String> sendNotification(Proposal payload) {
        return Mono.fromCallable(() -> buildRequest(payload))
                .flatMap(request -> Mono.fromFuture(client.sendMessage(request)))
                .doOnNext(response -> log.info("[SQSSender - NotificationLambda] Message sent {}", response.messageId()))
                .doOnError(error -> log.error("[SQSSender - NotificationLambda] Error while sending email with payload {} and with error {}",payload, error))
                .map(SendMessageResponse::messageId);
    }

    @Override
    public Mono<String> sendRequestAutomaticRevision(Proposal proposal) {
        return Mono.fromCallable(() -> buildAutomaticValidationRequest(proposal))
                .flatMap(request -> Mono.fromFuture(client.sendMessage(request)))
                .doOnNext(response -> log.info("[SQSSender - AutomaticValidationLambda] Message sent to automatic revision {}", response.messageId()))
                .doOnError(error -> log.error("[SQSSender - AutomaticValidationLambda] Error while sending automatic proposal to lambda {} and with error {}",proposal, error))
                .map(SendMessageResponse::messageId);
    }

    @Override
    public Mono<String> sendMetricsToReport(Double extraApprovedAmount) {
        return Mono.fromCallable(() -> buildReportMetricRequest(extraApprovedAmount))
                .flatMap(request -> Mono.fromFuture(client.sendMessage(request)))
                .doOnNext(response -> log.info("[SQSSender - ReportMetricsSQS] Message sent to report metrics {}", response.messageId()))
                .doOnError(error -> log.error("[SQSSender - ReportMetricsSQS] Error while sending metric to sqs and with error", error))
                .map(SendMessageResponse::messageId);
    }

    private SendMessageRequest buildAutomaticValidationRequest(Proposal proposal) {
        String messageBody = String.format(
                Locale.US,
                "{\"id\":%d,\"amount\":%.2f,\"proposalLimit\":%d,\"email\":\"%s\",\"interestRate\":%.2f,\"currentMonthlyDebt\":%.2f,\"baseSalary\":%.2f}",
                proposal.getId(),
                proposal.getAmount(),
                proposal.getProposalLimit(),
                proposal.getEmail(),
                proposal.getInterestRate(),
                proposal.getCurrentMonthlyDebt(),
                proposal.getBaseSalary()
        );
        return sendMessageRequest(messageBody, properties.queueAutomaticValidationUrl());
    }

    private SendMessageRequest buildReportMetricRequest(Double extraApprovedAmount) {
        String messageBody = String.format(
                Locale.US,
                "{\"amount\":%.2f}",
                extraApprovedAmount
        );
        return sendMessageRequest(messageBody, properties.queueReportMetrics());
    }

    private SendMessageRequest buildRequest(Proposal proposal) {
        StringBuilder loanPlanJson = getStringBuilder(proposal);

        String messageBody = String.format(
                Locale.US,
                "{" +
                        "\"email\":\"%s\"," +
                        "\"finalDecision\":\"%s\"," +
                        "\"id\":%d," +
                        "\"amount\":%.2f," +
                        "\"interestRate\":%.2f," +
                        "\"baseSalary\":%.2f," +
                        "\"currentMonthlyDebt\":%.2f," +
                        "\"maximumCapacity\":%.2f," +
                        "\"allowCapacity\":%.2f," +
                        "\"newMonthlyFee\":%.2f," +
                        "\"loanPlan\":%s" +
                        "}",
                proposal.getEmail(),
                proposal.getFinalDecision(),
                proposal.getId(),
                proposal.getAmount(),
                proposal.getInterestRate(),
                proposal.getBaseSalary(),
                proposal.getCurrentMonthlyDebt(),
                proposal.getMaximumCapacity(),
                proposal.getAllowCapacity(),
                proposal.getNewMonthlyFee(),
                loanPlanJson.toString()
        );

        return sendMessageRequest(messageBody, properties.queueSendNotificationUrl());
    }

    private static StringBuilder getStringBuilder(Proposal proposal) {
        StringBuilder loanPlanJson = new StringBuilder("[");
        int counter = 0;
        if(proposal.getFinalDecision().equals("APROBADO")){
            int size = proposal.getLoanPlan().size();
            for (Loan loan : proposal.getLoanPlan()) {
                loanPlanJson.append(String.format(
                        Locale.US,
                        "{\"quoteNumber\":%d,\"totalQuote\":%.2f,\"interests\":%.2f,\"capital\":%.2f,\"remainingBalance\":%.2f}",
                        loan.getQuoteNumber(),
                        loan.getTotalQuote(),
                        loan.getInterests(),
                        loan.getCapital(),
                        loan.getRemainingBalance()
                ));
                counter++;
                if (counter < size) {
                    loanPlanJson.append(",");
                }
            }
        }
        loanPlanJson.append("]");
        return loanPlanJson;
    }

    private SendMessageRequest sendMessageRequest(String messageBody, String queueUrl){
        return SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(messageBody)
                .build();
    }
}
