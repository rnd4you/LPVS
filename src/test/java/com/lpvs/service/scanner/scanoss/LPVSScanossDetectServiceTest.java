/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.service.scanner.scanoss;

import com.lpvs.entity.LPVSLicense;
import com.lpvs.entity.LPVSQueue;
import com.lpvs.repository.LPVSLicenseRepository;
import com.lpvs.service.LPVSGitHubService;
import com.lpvs.service.LPVSLicenseService;
import com.lpvs.util.LPVSWebhookUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class LPVSScanossDetectServiceTest {

    @Mock private LPVSQueue lpvsQueue;

    @Mock private LPVSLicenseService licenseService;

    @Mock private LPVSGitHubService gitHubService;

    @Mock private LPVSLicenseRepository lpvsLicenseRepository;

    private LPVSScanossDetectService scanossDetectService;

    @BeforeEach
    public void setUp() throws URISyntaxException, IOException {
        MockitoAnnotations.openMocks(this);
        String resourcePath = "A_B.json";
        String destinationPath =
                System.getProperty("user.home") + File.separator + "Results" + File.separator + "C";
        if (!(new File(destinationPath).exists())) {
            new File(destinationPath).mkdirs();
        }
        Files.copy(
                Paths.get(
                        Objects.requireNonNull(
                                        getClass().getClassLoader().getResource(resourcePath))
                                .toURI()),
                Paths.get(destinationPath + File.separator + resourcePath),
                StandardCopyOption.REPLACE_EXISTING);
        Mockito.when(licenseService.findLicenseBySPDX("MIT"))
                .thenReturn(
                        new LPVSLicense() {
                            {
                                setLicenseName("MIT");
                                setLicenseId(1L);
                                setSpdxId("MIT");
                            }
                        });
        scanossDetectService =
                new LPVSScanossDetectService(false, licenseService, lpvsLicenseRepository);
    }

    @AfterEach
    public void tearDown() {
        if ((new File(System.getProperty("user.home") + File.separator + "Results")).exists()) {
            new File(System.getProperty("user.home") + File.separator + "Results").delete();
        }
    }

    @Test
    public void testCheckLicense() {
        LPVSLicenseService licenseService = Mockito.mock(LPVSLicenseService.class);
        LPVSLicenseRepository lpvsLicenseRepository = Mockito.mock(LPVSLicenseRepository.class);
        LPVSScanossDetectService scanossDetectService =
                new LPVSScanossDetectService(false, licenseService, lpvsLicenseRepository);
        String licenseConflictsSource = "scanner";
        Mockito.when(LPVSWebhookUtil.getRepositoryName(lpvsQueue)).thenReturn("C");
        Mockito.when(lpvsQueue.getHeadCommitSHA()).thenReturn("A_B");
        Mockito.when(lpvsLicenseRepository.save(Mockito.any(LPVSLicense.class)))
                .thenAnswer(i -> i.getArguments()[0]);
        ReflectionTestUtils.setField(
                licenseService, "licenseConflictsSource", licenseConflictsSource);
        scanossDetectService.checkLicenses(lpvsQueue);
        Assertions.assertNotNull(scanossDetectService.checkLicenses(lpvsQueue));
    }

    @Test
    public void testCheckLicenseHeadCommitSHA() {
        LPVSLicenseService licenseService = Mockito.mock(LPVSLicenseService.class);
        LPVSLicenseRepository lpvsLicenseRepository = Mockito.mock(LPVSLicenseRepository.class);
        LPVSScanossDetectService scanossDetectService =
                new LPVSScanossDetectService(false, licenseService, lpvsLicenseRepository);
        String licenseConflictsSource = "scanner";
        LPVSQueue webhookConfig = Mockito.mock(LPVSQueue.class);
        Mockito.when(LPVSWebhookUtil.getRepositoryName(webhookConfig)).thenReturn("C");
        Mockito.when(webhookConfig.getHeadCommitSHA()).thenReturn("A_B");
        Mockito.when(lpvsLicenseRepository.save(Mockito.any(LPVSLicense.class)))
                .thenAnswer(i -> i.getArguments()[0]);
        ReflectionTestUtils.setField(
                licenseService, "licenseConflictsSource", licenseConflictsSource);
        webhookConfig.setHeadCommitSHA("");
        scanossDetectService.checkLicenses(webhookConfig);
        Assertions.assertNotNull(scanossDetectService.checkLicenses(webhookConfig));
    }

    @Test
    public void testWithNullHeadCommitSHA() {
        LPVSLicenseService licenseService = Mockito.mock(LPVSLicenseService.class);
        LPVSLicenseRepository lpvsLicenseRepository = Mockito.mock(LPVSLicenseRepository.class);
        LPVSScanossDetectService scanossDetectService =
                new LPVSScanossDetectService(false, licenseService, lpvsLicenseRepository);
        String licenseConflictsSource = "scanner";
        LPVSQueue webhookConfig = Mockito.mock(LPVSQueue.class);
        Mockito.when(LPVSWebhookUtil.getRepositoryName(webhookConfig)).thenReturn("A");
        Mockito.when(webhookConfig.getHeadCommitSHA()).thenReturn(null);
        Mockito.when(webhookConfig.getPullRequestUrl()).thenReturn("A/B");
        ReflectionTestUtils.setField(
                licenseService, "licenseConflictsSource", licenseConflictsSource);
        Assertions.assertNotNull(scanossDetectService.checkLicenses(webhookConfig));
    }

    @Test
    public void testWithNullHeadCommitSHADB() {
        String licenseConflictsSource = "db";
        LPVSQueue webhookConfig = Mockito.mock(LPVSQueue.class);
        Mockito.when(LPVSWebhookUtil.getRepositoryName(webhookConfig)).thenReturn("A");
        Mockito.when(webhookConfig.getHeadCommitSHA()).thenReturn(null);
        Mockito.when(webhookConfig.getPullRequestUrl()).thenReturn("A/B");
        ReflectionTestUtils.setField(
                licenseService, "licenseConflictsSource", licenseConflictsSource);
        Assertions.assertNotNull(scanossDetectService.checkLicenses(webhookConfig));
    }

    @Test
    public void testRunScan_StatusEqualsOne() throws Exception {
        Process process = Mockito.mock(Process.class);
        InputStream errorStream =
                new ByteArrayInputStream(
                        "Scanoss scanner terminated with none-zero code. Terminating.".getBytes());

        try (MockedConstruction<ProcessBuilder> mockedPb =
                Mockito.mockConstruction(
                        ProcessBuilder.class,
                        (mock, context) -> {
                            when(mock.inheritIO()).thenReturn(mock);
                            when(mock.start()).thenReturn(process);
                            when(process.getErrorStream()).thenReturn(errorStream);
                            when(process.waitFor()).thenReturn(1);
                        })) {
            Exception exception =
                    assertThrows(
                            Exception.class, () -> scanossDetectService.runScan(lpvsQueue, "path"));

            // Verify that the method throws an exception when the status is 1
            assertEquals(
                    "Scanoss scanner terminated with non-zero code. Terminating.",
                    exception.getMessage());

            verify(mockedPb.constructed().get(0)).start();
            verify(process, times(1)).waitFor();
        }
    }

    @Test
    public void testRunScan_StatusEqualsZero() throws Exception {
        Process process = Mockito.mock(Process.class);
        try (MockedConstruction<ProcessBuilder> mockedPb =
                Mockito.mockConstruction(
                        ProcessBuilder.class,
                        (mock, context) -> {
                            when(mock.inheritIO()).thenReturn(mock);
                            when(mock.start()).thenReturn(process);
                            when(process.waitFor()).thenReturn(0);
                        })) {
            scanossDetectService.runScan(lpvsQueue, "path");
            verify(mockedPb.constructed().get(0)).start();
            verify(process, times(1)).waitFor();
        }
    }
}
