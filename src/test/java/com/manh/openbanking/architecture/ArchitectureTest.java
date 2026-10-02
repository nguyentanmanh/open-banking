package com.manh.openbanking.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import com.manh.openbanking.application.port.out.TransactionHistoryProvider;
import com.manh.openbanking.application.port.in.TransactionHistoryUseCase;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.manh.openbanking")
class ArchitectureTest {
    @ArchTest static final ArchRule domain_is_framework_free = noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "..application..", "..adapter..", "..infrastructure..");
    @ArchTest static final ArchRule application_is_inside_adapters = noClasses().that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage("..adapter..", "..infrastructure..");
    @ArchTest static final ArchRule domain_exceptions_are_http_free = noClasses().that().resideInAPackage("..domain.exception..")
            .should().dependOnClassesThat().resideInAnyPackage("org.springframework.http..", "jakarta.servlet..");
    @ArchTest static final ArchRule controllers_do_not_access_outbound_ports = noClasses().that().haveSimpleNameEndingWith("Controller")
            .should().dependOnClassesThat().areAssignableTo(TransactionHistoryProvider.class);
    @ArchTest static final ArchRule controllers_access_inbound_port = classes().that().haveSimpleNameEndingWith("Controller")
            .should().dependOnClassesThat().areAssignableTo(TransactionHistoryUseCase.class);
    @ArchTest static final ArchRule outbound_adapters_implement_ports = classes().that().resideInAPackage("..adapter.out..")
            .and().haveSimpleNameEndingWith("Adapter").should().implement(TransactionHistoryProvider.class);
}
