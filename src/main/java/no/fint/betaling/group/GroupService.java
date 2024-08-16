package no.fint.betaling.group;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.common.exception.SchoolNotFoundException;
import no.fint.betaling.fintdata.SchoolRepository;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.felles.kompleksedatatyper.Periode;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.Link;
import no.fint.model.resource.utdanning.elev.ElevforholdResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Slf4j
@Service
public abstract class GroupService {

    //private final SchoolRepository schoolRepository;
//    private final TeachingGroupRepository teachingGroupRepository;
//    private final StudentRelationRepository studentRelationRepository;
//    private final StudentRepository studentRepository;
//    private final BasisGroupRepository basisGroupRepository;
//    private final ContactTeacherGroupRepository contactTeacherGroupRepository;
//    private final BasisGroupMembershipRepository basisGroupMembershipRepository;
//    private final ContactTeacherGroupMembershipRepository contactTeacherGroupMembershipRepository;
//    private final TeachingGroupMembershipRepository teachingGroupMembershipRepository;
//
//    public GroupService(SchoolRepository schoolRepository) {
//        this.schoolRepository = schoolRepository;
//        this.teachingGroupRepository = teachingGroupRepository;
//        this.studentRelationRepository = studentRelationRepository;
//        this.studentRepository = studentRepository;
//        this.basisGroupRepository = basisGroupRepository;
//        this.contactTeacherGroupRepository = contactTeacherGroupRepository;
//        this.basisGroupMembershipRepository = basisGroupMembershipRepository;
//        this.contactTeacherGroupMembershipRepository = contactTeacherGroupMembershipRepository;
//        this.teachingGroupMembershipRepository = teachingGroupMembershipRepository;
//    }

    protected SkoleResource getSchool(SchoolRepository schoolRepository, String schoolId) {
        return schoolRepository.get().stream()
                .filter(hasOrganisationNumber(schoolId))
                .findFirst()
                .orElseThrow(() -> new SchoolNotFoundException(String.format("x-school-org-id: %s", schoolId)));
    }

    protected boolean isActive(Periode gyldighetsperiode, Date today) {
        if (gyldighetsperiode == null) return true;
        if (gyldighetsperiode.getSlutt() == null) return true;
        return gyldighetsperiode.getSlutt().after(today);
    }

    protected Link getStudentLink(ElevforholdResource resource) {
        return resource.getElev().stream().findFirst().orElse(null);
    }

    protected Predicate<SkoleResource> hasOrganisationNumber(String schoolId) {
        return s -> Optional.ofNullable(s.getOrganisasjonsnummer())
                .map(Identifikator::getIdentifikatorverdi)
                .map(schoolId::equals)
                .orElse(false);
    }

    protected Link getElevforholdLink(FintLinks membership) {
        if (membership == null) {
            return null;
        }

        return membership.getLinks().get("elevforhold").stream().findFirst().orElse(null);
    }
}