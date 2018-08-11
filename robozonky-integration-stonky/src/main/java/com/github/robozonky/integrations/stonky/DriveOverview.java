/*
 * Copyright 2018 The RoboZonky Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.robozonky.integrations.stonky;

import java.io.IOException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.github.robozonky.api.SessionInfo;
import com.github.robozonky.common.state.InstanceState;
import com.github.robozonky.common.state.TenantState;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DriveOverview {

    private static final Logger LOGGER = LoggerFactory.getLogger(DriveOverview.class);

    private static final String MIME_TYPE_XLS_SPREADSHEET = "application/vnd.ms-excel";
    private static final String MIME_TYPE_FOLDER = "application/vnd.google-apps.folder";
    private static final String MIME_TYPE_GOOGLE_SPREADSHEET = "application/vnd.google-apps.spreadsheet";
    private static final String ROBOZONKY_INVESTMENTS_SHEET_NAME = "Export investic";
    private static final String ROBOZONKY_WALLET_SHEET_NAME = "Export peněženky";
    private static final String LATEST_SUPPORTED_STONKY_SPREADSHEET = "1MUhpUwFeTLXWOXS5ry1pNnSYG6GVNsiTmdRdBjAIVUw";
    private final InstanceState<DriveOverview> state;
    private final SessionInfo sessionInfo;
    private final Drive driveService;
    private volatile File folder;
    private volatile File investments;
    private volatile File wallet;

    private DriveOverview(final SessionInfo sessionInfo, final Drive driveService) {
        this(sessionInfo, driveService, null);
    }

    private DriveOverview(final SessionInfo sessionInfo, final Drive driveService, final File parent) {
        this(sessionInfo, driveService, parent, null);
    }

    private DriveOverview(final SessionInfo sessionInfo, final Drive driveService, final File parent,
                          final File wallet) {
        this(sessionInfo, driveService, parent, wallet, null);
    }

    private DriveOverview(final SessionInfo sessionInfo, final Drive driveService, final File parent, final File wallet,
                          final File investments) {
        this.sessionInfo = sessionInfo;
        this.driveService = driveService;
        this.folder = parent;
        this.investments = wallet;
        this.wallet = investments;
        this.state = TenantState.of(sessionInfo).in(DriveOverview.class);
    }

    private static String getFolderName(final SessionInfo sessionInfo) {
        return "Stonky pro účet '" + sessionInfo.getUsername() + "'";
    }

    public static DriveOverview create(final SessionInfo sessionInfo, final Drive driveService) throws IOException {
        LOGGER.debug("Querying Google for existence of parent folder.");
        final List<File> files = driveService.files().list().execute().getFiles();
        final Optional<File> result = files.stream()
                .filter(f -> Objects.equals(f.getMimeType(), MIME_TYPE_FOLDER))
                .filter(f -> Objects.equals(f.getName(), getFolderName(sessionInfo)))
                .findFirst();
        if (result.isPresent()) {
            return create(sessionInfo, driveService, result.get());
        } else {
            LOGGER.debug("Parent folder not found on Google.");
            return new DriveOverview(sessionInfo, driveService);
        }
    }

    private static Stream<File> listSpreadsheets(final List<File> all) {
        return all.stream()
                .filter(f -> Objects.equals(f.getMimeType(), MIME_TYPE_GOOGLE_SPREADSHEET));
    }

    private static Optional<File> getSpreadsheetWithName(final List<File> all, final String name) {
        return listSpreadsheets(all)
                .filter(f -> Objects.equals(f.getName(), name))
                .findFirst();
    }

    private static List<File> getFilesInFolder(final Drive driveService, final File parent) throws IOException {
        return driveService.files().list()
                .setQ("'" + parent.getId() + "' in parents")
                .execute().getFiles();
    }

    private static DriveOverview create(final SessionInfo sessionInfo, final Drive driveService,
                                        final File parent) throws IOException {
        LOGGER.debug("Querying Google for existence of wallet export.");
        final List<File> all = getFilesInFolder(driveService, parent);
        LOGGER.debug("Querying Google for existence of investment export.");
        return getSpreadsheetWithName(all, ROBOZONKY_WALLET_SHEET_NAME)
                .map(f -> createWithWallet(sessionInfo, driveService, all, parent, f))
                .orElseGet(() -> createWithoutWallet(sessionInfo, driveService, all, parent));
    }

    private static DriveOverview createWithWallet(final SessionInfo sessionInfo, final Drive driveService,
                                                  final List<File> all, final File parent, final File wallet) {
        return getSpreadsheetWithName(all, ROBOZONKY_INVESTMENTS_SHEET_NAME)
                .map(f -> new DriveOverview(sessionInfo, driveService, parent, wallet, f))
                .orElseGet(() -> new DriveOverview(sessionInfo, driveService, parent, wallet, null));
    }

    private static DriveOverview createWithoutWallet(final SessionInfo sessionInfo, final Drive driveService,
                                                     final List<File> all, final File parent) {
        return getSpreadsheetWithName(all, ROBOZONKY_INVESTMENTS_SHEET_NAME)
                .map(f -> new DriveOverview(sessionInfo, driveService, parent, null, f))
                .orElseGet(() -> new DriveOverview(sessionInfo, driveService, parent));
    }

    private static FileContent getFileContent(final URL sheet) {
        LOGGER.debug("Contacting Zonky to download the export.");
        return new FileContent(MIME_TYPE_XLS_SPREADSHEET, Util.download(sheet));
    }

    private File createRoboZonkyFolder(final Drive driveService) throws IOException {
        final File fileMetadata = new File();
        fileMetadata.setName(getFolderName(sessionInfo));
        fileMetadata.setDescription("Obsah tohoto adresáře aktualizuje RoboZonky, a to jednou denně." +
                                            "Adresář ani jeho obsah nemažte a pokud už to udělat musíte, " +
                                            "nezapomeňte ho odstranit také z Koše.");
        fileMetadata.setMimeType(MIME_TYPE_FOLDER);
        final File result = driveService.files().create(fileMetadata)
                .setFields("id")
                .execute();
        LOGGER.debug("Created a new Google folder '{}'.", result.getId());
        return result;
    }

    private File getOrCreateRoboZonkyFolder() throws IOException {
        if (folder == null) {
            folder = createRoboZonkyFolder(driveService);
        }
        return folder;
    }

    private File cloneStonky(final File upstream, final File parent) throws IOException {
        final File f = new File();
        f.setName(upstream.getName());
        f.setParents(Collections.singletonList(parent.getId()));
        final File result = driveService.files().copy(upstream.getId(), f)
                .setFields("id")
                .execute();
        state.update(m -> m.put("stonky", OffsetDateTime.now().toString()));
        LOGGER.debug("Created a copy of {} '{}'.", parent.getName(), result.getId());
        return result;
    }

    public File latestStonky() throws IOException {
        final File upstream = driveService.files().get(LATEST_SUPPORTED_STONKY_SPREADSHEET).execute();
        final File parent = getOrCreateRoboZonkyFolder();
        final Optional<File> stonky = listSpreadsheets(getFilesInFolder(driveService, parent))
                .filter(s -> Objects.equals(s.getName(), upstream.getName()))
                .findFirst();
        if (stonky.isPresent()) {
            return stonky.get();
        } else {
            return cloneStonky(upstream, parent);
        }
    }

    public File offerLatestWalletSpreadsheet(final Supplier<URL> sheet) throws IOException {
        LOGGER.debug("Processing wallet export.");
        wallet = uploadLatestSpreadsheet(driveService, wallet, sheet, ROBOZONKY_WALLET_SHEET_NAME);
        return wallet;
    }

    public File offerInvestmentsSpreadsheet(final Supplier<URL> sheet) throws IOException {
        LOGGER.debug("Processing investment export.");
        investments = uploadLatestSpreadsheet(driveService, investments, sheet, ROBOZONKY_INVESTMENTS_SHEET_NAME);
        return investments;
    }

    private File createSpreadsheet(final Supplier<URL> sheet, final String targetName) throws IOException {
        final File parent = getOrCreateRoboZonkyFolder();
        final File f = new File();
        f.setName(targetName);
        f.setParents(Collections.singletonList(parent.getId()));
        f.setMimeType(MIME_TYPE_GOOGLE_SPREADSHEET);
        final File result = driveService.files().create(f, getFileContent(sheet.get()))
                .setFields("id")
                .execute();
        state.update(m -> m.put(targetName, OffsetDateTime.now().toString()));
        LOGGER.debug("Created a new Google spreadsheet '{}'.", result.getId());
        return result;
    }

    private File modifySpreadsheet(final Drive driveService, final File original, final Supplier<URL> sheet,
                                   final String targetName) throws IOException {
        final boolean shouldUpdate = state.getValue(targetName)
                .map(value -> OffsetDateTime.parse(value).isBefore(OffsetDateTime.now().minusDays(1)))
                .orElse(true);
        if (shouldUpdate) {
            LOGGER.debug("Updating an existing Google spreadsheet '{}'.", original.getId());
            final File result = driveService.files().update(original.getId(), null, getFileContent(sheet.get()))
                    .setFields("id")
                    .execute();
            state.update(m -> m.put(targetName, OffsetDateTime.now().toString()));
            return result;
        } else {
            LOGGER.debug("Not touching the existing Google spreadsheet '{}'.", original.getId());
            return original;
        }
    }

    private File uploadLatestSpreadsheet(final Drive driveService, final File original,
                                         final Supplier<URL> sheet, final String targetName) throws IOException {
        return (original == null) ?
                createSpreadsheet(sheet, targetName) :
                modifySpreadsheet(driveService, original, sheet, targetName);
    }

    @Override
    public String toString() {
        return "DriveOverview{" +
                "folder=" + folder +
                ", investments=" + investments +
                ", wallet=" + wallet +
                '}';
    }
}
