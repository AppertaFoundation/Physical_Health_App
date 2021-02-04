package com.staircase13.apperta.service;

import org.junit.Test;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

public class UuidServiceTest {

    @Test
    public void randomUuid_areUnique() {
        UuidService service = new UuidService();

        Set<String> uuid = IntStream.range(0,10000).mapToObj(i -> service.randomUuid()).collect(Collectors.toSet());
        assertThat(uuid, hasSize(10000));
    }

}
