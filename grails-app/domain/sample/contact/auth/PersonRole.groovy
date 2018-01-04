package sample.contact.auth

import grails.gorm.DetachedCriteria
import groovy.transform.ToString

import org.apache.commons.lang.builder.HashCodeBuilder

@ToString(cache=true, includeNames=true, includePackage=false)
class PersonRole implements Serializable {

	private static final long serialVersionUID = 1

	Person person
	Role role

	PersonRole(Person p, Role r) {
		this()
		person = p
		role = r
	}

	@Override
	boolean equals(other) {
		if (!(other instanceof PersonRole)) {
			return false
		}

		other.person?.id == person?.id && other.role?.id == role?.id
	}

	@Override
	int hashCode() {
		def builder = new HashCodeBuilder()
		if (person) builder.append(person.id)
		if (role) builder.append(role.id)
		builder.toHashCode()
	}

	static PersonRole get(long personId, long roleId) {
		criteriaFor(personId, roleId).get()
	}

	static boolean exists(long personId, long roleId) {
		criteriaFor(personId, roleId).count()
	}

	private static DetachedCriteria criteriaFor(long personId, long roleId) {
		PersonRole.where {
			person == Person.load(personId) &&
			role == Role.load(roleId)
		}
	}

	static PersonRole create(Person person, Role role, boolean flush = false) {
		def instance = new PersonRole(person, role)
		instance.save(flush: flush, insert: true)
		instance
	}

	static boolean remove(Person p, Role r, boolean flush = false) {
		if (p == null || r == null) return false

		int rowCount = PersonRole.where { person == p && role == r }.deleteAll()

		if (flush) { PersonRole.withSession { it.flush() } }

		rowCount
	}

	static void removeAll(Person p, boolean flush = false) {
		if (p == null) return

		PersonRole.where { person == p }.deleteAll()

		if (flush) { PersonRole.withSession { it.flush() } }
	}

	static void removeAll(Role r, boolean flush = false) {
		if (r == null) return

		PersonRole.where { role == r }.deleteAll()

		if (flush) { PersonRole.withSession { it.flush() } }
	}

	static constraints = {
		role validator: { Role r, PersonRole pr ->
			if (pr.person == null || pr.person.id == null) return
			boolean existing = false
			PersonRole.withNewSession {
				existing = PersonRole.exists(pr.person.id, r.id)
			}
			if (existing) {
				return 'PersonRole.exists'
			}
		}
	}

	static mapping = {
		id composite: ['person', 'role']
		version false
	}
}
