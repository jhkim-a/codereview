package io.frebigbird.example.charon.proxy.token;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
public class OAuthTokenSupplierTest {
    @Test
    public void tokenTest() {
        OAuthTokenSupplier tokenSupplier = new OAuthTokenSupplier(new OAuthTokenSupplierProperties());
        System.out.println("token : " + tokenSupplier.getToken());
        Assertions.assertThat(tokenSupplier.getToken()).isNotEmpty();
    }
}
