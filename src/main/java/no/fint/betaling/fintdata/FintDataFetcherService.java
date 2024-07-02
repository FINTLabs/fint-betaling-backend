package no.fint.betaling.fintdata;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.okonomi.faktura.FakturautstederResources;
import no.fint.model.resource.okonomi.kodeverk.VareResources;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@Service
public class FintDataFetcherService {

    @Scheduled(initialDelay = 1000L, fixedDelayString = "${fint.betaling.refresh-rate:3600000}")
    public void updateAll() {
        // TODO: Implement this method
//        int basisGroupsUpdated = basisGroups.update();
//        int contactTeachersUpdated = contactTeacherGroups.update();
//        int studentRelationsUpdated = studentRelations.update();
//        int studentsUpdated = students.update();
//        log.info("Updates completed, basisgruppe ({}), undervisningsgruppe ({}), elevforhold ({}), elev ({})", basisGroupsUpdated, contactTeachersUpdated, studentRelationsUpdated, studentsUpdated);

        // fra schools:
//        log.info("Updates completed, schools ({}), schoolresource ({})", schoolsUpdated, schoolResourceUpdated);


        //@Scheduled(initialDelay = 1000L, fixedDelayString = "${fint.betaling.refresh-rate:1200000}")
        //public void updateTaxcodes() {

        //@Scheduled(initialDelay = 1000L, fixedDelayString = "${fint.betaling.refresh-rate:1200000}")
        //public void updateLineitems() {

        // I FintResourceRepository må vi se på retry:
//        restUtil.getUpdates(VareResources.class, endpoints.getVare())
//                .retryWhen(
//                        Retry.backoff(5, Duration.ofSeconds(10))
//                                .maxBackoff(Duration.ofSeconds(60)))


//        @Scheduled(initialDelay = 1000L, fixedDelayString = "${fint.betaling.refresh-rate:1200000}")
//        public void updateInvoiceIssuers() {
//            log.info("Updating invoice issuer from {} ...", endpoints.getInvoiceIssuer());
//            restUtil.getUpdates(FakturautstederResources.class, endpoints.getInvoiceIssuer())


    }

}
