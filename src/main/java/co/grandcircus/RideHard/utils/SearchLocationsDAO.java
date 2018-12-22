package co.grandcircus.RideHard.utils;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.springframework.stereotype.Repository;

@Repository
@Transactional
public class SearchLocationsDAO {

	@PersistenceContext
	private EntityManager em;

	public List<Country> findAllCountries() {
		return em.createQuery("FROM Country", Country.class).getResultList();

	}

	public List<State> findAllStates() {
		return em.createQuery("FROM State", State.class).getResultList();
	}
}