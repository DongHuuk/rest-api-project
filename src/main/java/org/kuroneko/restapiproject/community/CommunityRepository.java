package org.kuroneko.restapiproject.community;

import org.kuroneko.restapiproject.community.domain.Community;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityRepository extends JpaRepository<Community, String> {

}
