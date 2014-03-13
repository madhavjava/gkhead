package org.mifos.customers.client.business;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mifos.config.ClientRules;
import org.mifos.dto.screen.ClientNameDetailDto;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ClientNameDetailDtoTest {

    @Test
    public void shouldGetDisplayName() {
        ClientNameDetailDto clientNameDetailDto = new ClientNameDetailDto();
        clientNameDetailDto.setNames(ClientRules.getNameSequence());

        clientNameDetailDto.setFirstName(" First  ");
        clientNameDetailDto.setLastName(" Last ");
        assertThat(clientNameDetailDto.getDisplayName(), is("First Last"));
    }
}