package com.sap.lm.sl.spfi.refapp.mocks;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class IntegrationTestsTest {

    @Inject
    private IntegrationTests integrationTests;

    @Test
    public void isTestTenantByName() {
        boolean isTestTenant;
        isTestTenant = integrationTests.isTestTenantByName(
            "PNRhzUFMATTF6KvZdb0QpZkyVaTDCh3YDdaOpD80Nb9RguGf2UBHoAqSt5jnexWN9NLFckOUXdy920LYeU8VQ+aVVyEJV8qq2Wb4LxZSOS4nn5auP1WuOFcyUk17DWrr6DxHn1cJVEwvgmIMUJcjfJxXU5zcqUdGgu23i+vbAvXW6axdWXSOwEjPBlC7985YIqjZTQk50fpv8/efafgEQa8dZ575BYovk908TP34PnSAPpaRYCF7Dw3skOW/1feY7uRxZ+aHAKWoPUlVghGzQU1GXk3q9+Cv0MH3KBTSJXXtkH/2wka2anzHGz3gls4uWekpOASP80ODoRJLKMGMTa9+f1oSfowZsU8l0BAFcGkfSLhpLikVveVpIlUl160TmCaMmhKy1E5TUdXEyQ8qXVhTw9Zq3e+mfREN2rKdLwPff6HRa0rD0+gXbpKXrjt7s1GWiErWTPEp8q3FOeSy/GXE8Ho7JXreziNbI3r+PM70TJNsJMvd1mpygK97EfYUzPPPU/iGrs33XBodyL61bnnRcYTthXaYWrGQkVtinW91ibxxrTFIXrSyxABwRmDVsDS52egU14tBqsPHSSjtsGWXq3lSHNu/6pQ7yjapQchGlc5nhlH3TxmTg6wUq3IYuQDuUL0pPyGo1T4ov6dgff4TGyyGU/7tju0kmDiadtoyitWgkDke54LGsZG7uEAh/cRW5pgDrRrZR4RIfFMYNjmJ8hQVujDcsODXGY7x1GPnWuL92XK5MEITb2UTEqlcV+2ifioyOXaR1pbkp2gT5Q==", 
            "https://saas-ops-service.stag-op.spcnatprov.shoot.live.k8s-hana.ondemand.com");
        assertFalse(isTestTenant);
        isTestTenant = integrationTests.isTestTenantByName(
            "eDM2zU5pwGPtp+XKOnKK4xDAkcRYbHsNX8rcsAXLzpgweVkNWDLJTDeip3Bv3D105kL5Arg0TTHY/vPABDnw8EVZoYgRNS44YOGEcZkNLdnH7Wc7C1ZgD+YFNQC//Ek+wm+lNrQCHwVgWpOMuxgsWoAOddLUR3L/yEiMbhj3fDe7Ym2ENOw8fWlhPyjq/op64XFWh89eY63KiNEKaMJtqMaQL0E/hlWax0q9IHtQ9PhqbMNbj7FrPaYxUjjY5P907JlP2S9OrwZ3WDJbCThxfoB82LJe67y+zAl45xeYZHmZHtvjjYuaLo1nw+AIpmHprLIY90R3XN1R8r5F8U2Usj3lKr4anTKm93YAjNtQg+5A+9H2yMrbGB0t3b/dhiY47/w3GMmAo0Qp7PJTrFdGIZ/QgNMevdrwCcNH+aWOvTVX54vNcXcd+EGKQjPI+3i1rDi1pNpknOK5uEHw4h5p8dO5BfGf6MPvP+7O27nIm+yBwqDrrL53uky3MBBrRLQlr6KXa9DV9sZl6UgWoJXxzr2juAu4d+UoQknU48fMJ03nnBClxsTm7hKOCZrge5oHTvHvrjls0ZsZNG6xxQcXhIx/kIwbPLOEg6jfOopjSc1NITuOzkY/l672XXxvAe4IsgoA5N4E4xhqU1wgNEm6ibdrPG/eZ+9f+AbtDEXdss9QAeI4Is2xeuC4qQpBKJ4G", 
            "https://saas-ops-service.dev-op.spcnatprov.shoot.live.k8s-hana.ondemand.com");
        assertFalse(isTestTenant);
        isTestTenant = integrationTests.isTestTenantByName(
            "eDM2zU5pwGPtp+XKOnKK4xDAkcRYbHsNX8rcsAXLzpgweVkNWDLJTDeip3Bv3D105kL5Arg0TTHY/vPABDnw8EVZoYgRNS44YOGEcZkNLdnH7Wc7C1ZgD+YFNQC//Ek+wm+lNrQCHwVgWpOMuxgsWoAOddLUR3L/yEiMbhj3fDe7Ym2ENOw8fWlhPyjq/op64XFWh89eY63KiNEKaMJtqMaQL0E/hlWax0q9IHtQ9PhqbMNbj7FrPaYxUjjY5P907JlP2S9OrwZ3WDJbCThxfoB82LJe67y+zAl45xeYZHmZHtvjjYuaLo1nw+AIpmHprLIY90R3XN1R8r5F8U2Usj3lKr4anTKm93YAjNtQg+5A+9H2yMrbGB0t3b/dhiY4q8bPjZB7rOxyAGWnPWUzj5ivT5AePyaYlgBjzO2VwG61pgNZyaqSrmT9tapN41hW6wAaC3RD5M5lFHORC/ND9oYjecst3dr0OimSdHjsPhq9T/3BB3vVs74phDCklLdoMRuk04o4Q1mNevBPTQTV4usTUjnsQCs9xF4gaxsKVni4jGJZyHGysH5BMqD+pPm16om6tuA6jt6VQDYihLNsd7cC1l8r1qWDYREZWcR5z9a6NKf4BIuqQ3O4CsbzsyOoztiJRSukHTQV582Yw3rGW/N4+6OFpbDRT1hlnzvoKxmbG4CBGXiNmjFqlJDRuThM", 
            "https://saas-ops-service.dev-op.spcnatprov.shoot.live.k8s-hana.ondemand.com");
        assertTrue(isTestTenant);
    }

    @Test
    public void isNotificationFailTestTenant() {
        boolean isTestTenant;
        isTestTenant = integrationTests.isNotificationFailTestTenant(
                "PNRhzUFMATTF6KvZdb0QpZkyVaTDCh3YDdaOpD80Nb9RguGf2UBHoAqSt5jnexWN9NLFckOUXdy920LYeU8VQ+aVVyEJV8qq2Wb4LxZSOS4nn5auP1WuOFcyUk17DWrr6DxHn1cJVEwvgmIMUJcjfJxXU5zcqUdGgu23i+vbAvXW6axdWXSOwEjPBlC7985YIqjZTQk50fpv8/efafgEQa8dZ575BYovk908TP34PnSAPpaRYCF7Dw3skOW/1feY7uRxZ+aHAKWoPUlVghGzQU1GXk3q9+Cv0MH3KBTSJXXtkH/2wka2anzHGz3gls4uWekpOASP80ODoRJLKMGMTa9+f1oSfowZsU8l0BAFcGkfSLhpLikVveVpIlUl160TmCaMmhKy1E5TUdXEyQ8qXVhTw9Zq3e+mfREN2rKdLwPff6HRa0rD0+gXbpKXrjt7s1GWiErWTPEp8q3FOeSy/GXE8Ho7JXreziNbI3r+PM70TJNsJMvd1mpygK97EfYUzPPPU/iGrs33XBodyL61bnnRcYTthXaYWrGQkVtinW91ibxxrTFIXrSyxABwRmDVsDS52egU14tBqsPHSSjtsGWXq3lSHNu/6pQ7yjapQchGlc5nhlH3TxmTg6wUq3IYuQDuUL0pPyGo1T4ov6dgff4TGyyGU/7tju0kmDiadtoyitWgkDke54LGsZG7uEAh/cRW5pgDrRrZR4RIfFMYNjmJ8hQVujDcsODXGY7x1GPnWuL92XK5MEITb2UTEqlcV+2ifioyOXaR1pbkp2gT5Q==",
                "https://saas-ops-service.stag-op.spcnatprov.shoot.live.k8s-hana.ondemand.com");
        assertFalse(isTestTenant);

        isTestTenant = integrationTests.isNotificationFailTestTenant(
                "PNRhzUFMATTF6KvZdb0QpZkyVaTDCh3YDdaOpD80Nb9RguGf2UBHoAqSt5jnexWN9NLFckOUXdy920LYeU8VQ+aVVyEJV8qq2Wb4LxZSOS4nn5auP1WuOFcyUk17DWrr6DxHn1cJVEwvgmIMUJcjfJxXU5zcqUdGgu23i+vbAvXW6axdWXSOwEjPBlC7985YIqjZTQk50fpv8/efafgEQa8dZ575BYovk908TP34PnSAPpaRYCF7Dw3skOW/1feY7uRxZ+aHAKWoPUlVghGzQU1GXk3q9+Cv0MH3KBTSJXXtkH/2wka2anzHGz3gls4uWekpOASP80ODoRJLKMGMTa9+f1oSfowZsU8l0BAFcGkfSLhpLikVveVpIlUl160TmCaMmhKy1E5TUdXEyQ8qXVhTw9Zq3e+mfREN2rKdLwPff6HRa0rD0+gXbpKXrjt7s1GWiErWTPEp8q3FOeSy/Ie7bI23G6MbeunWKLs7S3qI9sW11e/oA7P7wo/6M/XkacT266Gj3NMuyN+BEHqK8aFf5Psgwe6Ap6r//MOfu0/GKZ80VLb6wX6B4e6bVcuHkhsU7xbkIs0zMSex/PYV4KNci8Cr66h+fLY0QzGuw3VeEHWMlSugXJ1vScZmMQDR9LclPq/N4obJu7TOqWbxgKXyIu6Jhk1tCGr/HwGNawTJ/RDVyAkfyx/HHOpVT0cBnGsrAhRpCLaf0Ga+FKtRKl5ULG3r3uS3UtoLRyxRtIk=",
                "https://saas-ops-service.stag-op.spcnatprov.shoot.live.k8s-hana.ondemand.com");
        assertTrue(isTestTenant);
    }

    @Test
    public void isActivationFailTestTenant() {
        boolean isTestTenant;
        isTestTenant = integrationTests.isActivationFailTestTenant(
                "PNRhzUFMATTF6KvZdb0QpZkyVaTDCh3YDdaOpD80Nb9RguGf2UBHoAqSt5jnexWN9NLFckOUXdy920LYeU8VQ+aVVyEJV8qq2Wb4LxZSOS4nn5auP1WuOFcyUk17DWrr6DxHn1cJVEwvgmIMUJcjfJxXU5zcqUdGgu23i+vbAvXW6axdWXSOwEjPBlC7985YIqjZTQk50fpv8/efafgEQa8dZ575BYovk908TP34PnSAPpaRYCF7Dw3skOW/1feY7uRxZ+aHAKWoPUlVghGzQU1GXk3q9+Cv0MH3KBTSJXXtkH/2wka2anzHGz3gls4uWekpOASP80ODoRJLKMGMTa9+f1oSfowZsU8l0BAFcGkfSLhpLikVveVpIlUl160TmCaMmhKy1E5TUdXEyQ8qXVhTw9Zq3e+mfREN2rKdLwPff6HRa0rD0+gXbpKXrjt7s1GWiErWTPEp8q3FOeSy/GXE8Ho7JXreziNbI3r+PM70TJNsJMvd1mpygK97EfYUzPPPU/iGrs33XBodyL61bnnRcYTthXaYWrGQkVtinW91ibxxrTFIXrSyxABwRmDVsDS52egU14tBqsPHSSjtsGWXq3lSHNu/6pQ7yjapQchGlc5nhlH3TxmTg6wUq3IYuQDuUL0pPyGo1T4ov6dgff4TGyyGU/7tju0kmDiadtoyitWgkDke54LGsZG7uEAh/cRW5pgDrRrZR4RIfFMYNjmJ8hQVujDcsODXGY7x1GPnWuL92XK5MEITb2UTEqlcV+2ifioyOXaR1pbkp2gT5Q==",
                "https://saas-ops-service.stag-op.spcnatprov.shoot.live.k8s-hana.ondemand.com");
        assertFalse(isTestTenant);

        isTestTenant = integrationTests.isActivationFailTestTenant(
                "PNRhzUFMATTF6KvZdb0QpZkyVaTDCh3YDdaOpD80Nb9RguGf2UBHoAqSt5jnexWN9NLFckOUXdy920LYeU8VQ+aVVyEJV8qq2Wb4LxZSOS4nn5auP1WuOFcyUk17DWrr6DxHn1cJVEwvgmIMUJcjfJxXU5zcqUdGgu23i+vbAvXW6axdWXSOwEjPBlC7985YIqjZTQk50fpv8/efafgEQa8dZ575BYovk908TP34PnSAPpaRYCF7Dw3skOW/1feY7uRxZ+aHAKWoPUlVghGzQU1GXk3q9+Cv0MH3KBTSJXXtkH/2wka2anzHGz3gls4uWekpOASP80ODoRJLKMGMTa9+f1oSfowZsU8l0BAFcGkfSLhpLikVveVpIlUl160TmCaMmhKy1E5TUdXEyQ8qXVhTw9Zq3e+mfREN2rKdLwPff6HRa0rD0+gXbpKXrjt7s1GWiErWTPEp8q3FOeSy/Ie7bI23G6MbeunWKLs7S3qI9sW11e/oA7P7wo/6M/XkacT266Gj3NMuyN+BEHqK8WHZADBD9dcEjlfbwDfG1bED4PQEA9Atg45WhS5gwv6BM0BUs1NqVhUkCVjLJp6UcyglkEbXixdQL2RrQHqNFnSiFsb3bFcmbaqZlMhBVvAeGz309I66xk1F5iR+JtwO31wx8vk5LTtsfQ2APMPdg3FtrZeWPHW2p6st4rWeSchYvI0vgrT2Djg6Qj35cv9w9DFLMSFmHTGwfMRkde5Dr5U=",
                "https://saas-ops-service.stag-op.spcnatprov.shoot.live.k8s-hana.ondemand.com");
        assertTrue(isTestTenant);
    }
}
