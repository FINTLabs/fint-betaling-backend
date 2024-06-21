package no.fint.betaling.group;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.common.exception.SchoolNotFoundException;
import no.fint.betaling.model.Customer;
import no.fint.betaling.model.CustomerGroup;
import no.fint.betaling.organisation.CustomerFactory;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.felles.kompleksedatatyper.Periode;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.Link;
import no.fint.model.resource.felles.PersonResource;
import no.fint.model.resource.utdanning.basisklasser.GruppeResource;
import no.fint.model.resource.utdanning.elev.ElevforholdResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GroupService {

	private final GroupRepository groupRepository;

	public GroupService(GroupRepository groupRepository) {
		this.groupRepository = groupRepository;
	}

	private SkoleResource getSchool(String schoolId) {
		return groupRepository.getSchools().values().stream()
				.filter(hasOrganisationNumber(schoolId))
				.findFirst()
				.orElseThrow(() -> new SchoolNotFoundException(String.format("x-school-org-id: %s", schoolId)));
	}

	public CustomerGroup getCustomerGroupBySchool(String schoolId) {
		SkoleResource school = getSchool(schoolId);

		return createCustomerGroup(school);
	}

	public Map<String, Customer> getCustomersForSchoolWithVisIdKey(String schoolId) {
		return getSchool(schoolId)
				.getElevforhold()
				.stream()
				.map(groupRepository.getStudentRelations()::get)
				.filter(e -> e != null && e.getSystemId() != null && e.getSystemId().getIdentifikatorverdi() != null)
				.collect(Collectors.toMap(
						e -> e.getSystemId().getIdentifikatorverdi(),
						e -> e.getElev()
								.stream()
								.filter(Objects::nonNull)
								.map(groupRepository.getStudents()::get)
								.map(CustomerFactory::toCustomer)
								.distinct()
								.findFirst()
								.orElseThrow(IllegalStateException::new)
				));
	}

	public List<CustomerGroup> getCustomerGroupsByBasisGroupsAndSchool(String schoolId) {
		SkoleResource school = getSchool(schoolId);

		return school.getBasisgruppe().stream()
				.map(group -> groupRepository.getBasisGroups().get(group))
				.filter(Objects::nonNull)
				.map(this::createCustomerGroup)
				.collect(Collectors.toList());
	}

	public List<CustomerGroup> getCustomerGroupsByTeachingGroupsAndSchool(String schoolId) {
		SkoleResource school = getSchool(schoolId);

		return school.getUndervisningsgruppe().stream()
				.map(group -> groupRepository.getTeachingGroups().get(group))
				.filter(Objects::nonNull)
				.map(this::createCustomerGroup)
				.collect(Collectors.toList());
	}

	public List<CustomerGroup> getCustomerGroupsByContactTeacherGroupsAndSchool(String schoolId) {
		SkoleResource school = getSchool(schoolId);

		return school.getKontaktlarergruppe().stream()
				.map(group -> groupRepository.getContactTeacherGroups().get(group))
				.filter(Objects::nonNull)
				.map(this::createCustomerGroup)
				.collect(Collectors.toList());
	}

	private <T extends GruppeResource & FintLinks> CustomerGroup createCustomerGroup(T group) {
		CustomerGroup customerGroup = new CustomerGroup();
		customerGroup.setName(group.getNavn());
		customerGroup.setDescription(group.getBeskrivelse());
		customerGroup.setCustomers(getCustomersForGroup(group.getLinks().get("elevforhold")));
		return customerGroup;
	}

	private CustomerGroup createCustomerGroup(SkoleResource school) {
		CustomerGroup customerGroup = new CustomerGroup();
		customerGroup.setName(school.getNavn());
		customerGroup.setDescription(null);
		customerGroup.setCustomers(getCustomersForGroup(school.getElevforhold()));
		return customerGroup;
	}

	private List<Customer> getCustomersForGroup(List<Link> studentRelationLinks) {
		if (studentRelationLinks == null) {
			return Collections.emptyList();
		}
		Map<Link, ElevforholdResource> studentRelations = groupRepository.getStudentRelations();
		Map<Link, PersonResource> students = groupRepository.getStudents();
		final Date today = new Date();

		return studentRelationLinks.stream()
				.map(studentRelations::get)
				.filter(Objects::nonNull)
				.filter(studentRelation -> isActive(studentRelation, today))
				.map(this::getStudentLink)
				.filter(Objects::nonNull)
				.map(students::get)
				.filter(Objects::nonNull)
				.map(CustomerFactory::toCustomer)
				.distinct()
				.collect(Collectors.toList());
	}

	private boolean isActive(ElevforholdResource studentRelation, Date today) {
		Periode gyldighetsperiode = studentRelation.getGyldighetsperiode();
		if (gyldighetsperiode == null) return true;
		if (gyldighetsperiode.getSlutt() == null) return true;
		return gyldighetsperiode.getSlutt().after(today);
	}

	private Link getStudentLink(ElevforholdResource resource) {
		return resource.getElev().stream().findFirst().orElse(null);
	}

	private Predicate<SkoleResource> hasOrganisationNumber(String schoolId) {
		return s -> Optional.ofNullable(s.getOrganisasjonsnummer())
				.map(Identifikator::getIdentifikatorverdi)
				.map(schoolId::equals)
				.orElse(false);
	}
}