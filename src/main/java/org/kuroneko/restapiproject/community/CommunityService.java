package org.kuroneko.restapiproject.community;

import org.kuroneko.restapiproject.account.domain.Account;
import org.kuroneko.restapiproject.community.domain.Community;
import org.kuroneko.restapiproject.community.domain.CommunityForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CommunityService {

    @Autowired CommunityRepository communityRepository;

    public Community createCommunity(CommunityForm communityForm, Account account) {
        Community community = new Community();
        community.setTitle(communityForm.getTitle());
        community.setManager(account);
        community.setCreateTime(LocalDateTime.now());
        Community newCommunity = communityRepository.save(community);
        account.getCommunities().add(newCommunity);
        return newCommunity;
    }

    public void deleteCommunity(Community community) {
        this.communityRepository.delete(community);
    }
}
