package com.hiresync.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class UserDetailsImplTest {

    @Test
    @DisplayName("equals: id가 같으면 true를 반환한다")
    void equals_SameId_ReturnsTrue() {
        // given
        Long id = 1L;
        UserDetailsImpl user1 = new UserDetailsImpl(id, "user@test.com", "password", 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        UserDetailsImpl user2 = new UserDetailsImpl(id, "other@test.com", "otherpassword", 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));

        // when & then
        assertThat(user1.equals(user2)).isTrue();
    }

    @Test
    @DisplayName("equals: id가 다르면 false를 반환한다")
    void equals_DifferentId_ReturnsFalse() {
        // given
        UserDetailsImpl user1 = new UserDetailsImpl(1L, "user@test.com", "password", 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        UserDetailsImpl user2 = new UserDetailsImpl(2L, "user@test.com", "password", 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        // when & then
        assertThat(user1.equals(user2)).isFalse();
    }

    @Test
    @DisplayName("equals: 동일한 객체인 경우 true를 반환한다")
    void equals_SameObject_ReturnsTrue() {
        // given
        UserDetailsImpl user = new UserDetailsImpl(1L, "user@test.com", "password", 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        // when & then
        assertThat(user.equals(user)).isTrue();
    }

    @Test
    @DisplayName("equals: 비교 대상이 null인 경우 false를 반환한다")
    void equals_NullObject_ReturnsFalse() {
        // given
        UserDetailsImpl user = new UserDetailsImpl(1L, "user@test.com", "password", 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        // when & then
        assertThat(user.equals(null)).isFalse();
    }

    @Test
    @DisplayName("equals: 비교 대상의 클래스 타입이 다른 경우 false를 반환한다")
    void equals_DifferentClass_ReturnsFalse() {
        // given
        UserDetailsImpl user = new UserDetailsImpl(1L, "user@test.com", "password", 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        Object otherObject = new Object();

        // when & then
        assertThat(user.equals(otherObject)).isFalse();
    }

    @Test
    @DisplayName("hashCode: id가 같으면 동일한 hashCode를 반환한다")
    void hashCode_SameId_ReturnsSameHashCode() {
        // given
        Long id = 1L;
        UserDetailsImpl user1 = new UserDetailsImpl(id, "user@test.com", "password", 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        UserDetailsImpl user2 = new UserDetailsImpl(id, "other@test.com", "otherpassword", 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));

        // when & then
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }

    @Test
    @DisplayName("hashCode: id가 다르면 다른 hashCode를 반환할 가능성이 높다")
    void hashCode_DifferentId_ReturnsDifferentHashCode() {
        // given
        UserDetailsImpl user1 = new UserDetailsImpl(1L, "user@test.com", "password", 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        UserDetailsImpl user2 = new UserDetailsImpl(2L, "user@test.com", "password", 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        // when & then
        assertThat(user1.hashCode()).isNotEqualTo(user2.hashCode());
    }
}
