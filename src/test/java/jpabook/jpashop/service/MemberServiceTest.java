package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest
@RunWith(SpringRunner.class)
@Transactional
class MemberServiceTest {

    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;

    @Test
    void join() {
        Member member = new Member();
        member.setName("memberA");

        Long saveId = memberService.join(member);

        assertThat(member).isEqualTo(memberRepository.findOne(saveId));
    }

    @Test
    void findMembers() {
        Member member1 = new Member();
        member1.setName("memberA");

        Member member2 = new Member();
        member2.setName("memberA");

        memberService.join(member1);

        assertThrows(IllegalStateException.class, () -> {
            memberService.join(member1);
        });
    }
}