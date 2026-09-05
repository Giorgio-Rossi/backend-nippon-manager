-- Tesserati Nippon Judo Codogno, stagione 2025/26.
-- Estratto dal gestionale FIJLKAM ("Tesserati anno 25-26.xlsx", 57 tessere -> 50 atleti).
-- Gli INSERT sono condizionati sul codice fiscale: la migrazione non duplica
-- un atleta gia presente in archivio.

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Claudia', 'Bernabe''', '2008-09-01', 'BRNCLD08P41C816V',
       'berna.claudia08@gmail.com', NULL,
       'VIA GARIBALDI', 'Codogno',
       '2026-01-26', 'Nera (1° DAN)',
       'Tesseramento FIJLKAM 25/26 n. 572234 (Atleta - AG (Femminile))', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'BRNCLD08P41C816V');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Alessandro', 'Bonvini', '2017-08-14', 'BNVLSN17M14G535Z',
       'marina.maietti@virgilio.it', '3923711792',
       'vicoloAngelo Clavena 10', 'Codogno',
       '2025-10-08', 'Bianca',
       'Tesseramento FIJLKAM 25/26 n. 1070465 (Atleta - PA)', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'BNVLSN17M14G535Z');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Ahmed Ayoub', 'Boutayeb', '2012-03-01', 'BTYHDY12C01Z330U',
       NULL, '3886342047',
       'vicolo bezzecca', 'Codogno',
       '2025-10-06', 'Bianca',
       'Tesseramento FIJLKAM 25/26 n. 1048520 (Atleta Amatore)', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'BTYHDY12C01Z330U');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Andrei', 'Bucataru', '2012-01-27', 'BCTNRN12A27E648K',
       NULL, '3292789730',
       'VIA CASCINI', 'Terranova dei Passerini',
       '2026-01-26', 'Arancione',
       'Tesseramento FIJLKAM 25/26 n. 985204 (Atleta - AG (Maschile))', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'BCTNRN12A27E648K');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Ettore', 'Cantoni', '2020-09-01', 'CNTTTR20P01G535V',
       NULL, '3386758191',
       'Via Elsa morante 17 G', 'Codogno',
       '2026-02-23', 'Bianca',
       'Tesseramento FIJLKAM 25/26 n. 1083615 (Atleta - PA)', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'CNTTTR20P01G535V');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Sofia', 'Caridi', '2020-01-15', 'CRDSFO20A55G535D',
       NULL, '3703222748',
       'Via Italo Svevo 16I', 'Codogno',
       '2025-10-13', 'Bianca',
       'Tesseramento FIJLKAM 25/26 n. 1055465 (Atleta - PA)', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'CRDSFO20A55G535D');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Jacopo', 'Carnevale Cortis', '2020-09-15', 'CRNJCP20P15G535Y',
       NULL, '3392048696',
       'viale risorgimento,16', 'Codogno',
       '2025-10-20', 'Bianca',
       'Tesseramento FIJLKAM 25/26 n. 1042286 (Atleta - PA)', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'CRNJCP20P15G535Y');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Marco', 'Codazzi', '2016-08-18', 'CDZMRC16M18C816E',
       NULL, '393921462597',
       'Via Francesco Petrarca', 'Codogno',
       '2026-01-14', 'Gialla',
       'Tesseramento FIJLKAM 25/26 n. 1019157 (Atleta - PA)', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'CDZMRC16M18C816E');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Giulia', 'Coldani', '2008-08-12', 'CLDGLI08M52C816L',
       'coldani2008@gmail.com', '3334842814',
       'VIA SN PAOLO', 'Casalpusterlengo',
       '2026-01-26', 'Marrone',
       'Tesseramento FIJLKAM 25/26 n. 1034310 (Atleta - AG (Femminile))', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'CLDGLI08M52C816L');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Tabata', 'Danni', '2019-07-07', 'DNNTBT19L47A944Q',
       NULL, '3386722625',
       'Viale risorgimento 34', 'Codogno',
       '2025-10-20', 'Bianca',
       'Tesseramento FIJLKAM 25/26 n. 1055467 (Atleta - PA)', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'DNNTBT19L47A944Q');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Davide', 'Della Giovanna', '1987-12-23', 'DLLDVD87T23C816U',
       'dellag.davide@gmail.com', '037785363',
       'VIA GULF ITALIANA 9', 'Terranova dei Passerini',
       '2026-03-19', 'Marrone',
       'Tesseramento FIJLKAM 25/26 n. 36934 (Atleta Amatore)', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'DLLDVD87T23C816U');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Damiano', 'Di Leva', '2017-01-13', 'DLVDMN17A13C816L',
       NULL, '3425533277',
       'viale albino, 2', 'Codogno',
       '2025-10-08', 'Bianca',
       'Tesseramento FIJLKAM 25/26 n. 1070459 (Atleta - PA)', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'DLVDMN17A13C816L');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Ahmad', 'Ettalbi', '2018-05-24', 'TTLHMD18E24E648F',
       'ettalbitouria@gmail.com', '34516290959',
       'VIA, PO', 'Codogno',
       '2025-11-07', 'Gialla',
       'Tesseramento FIJLKAM 25/26 n. 920698 (Atleta - PA)', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'TTLHMD18E24E648F');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Adam', 'Fouaji', '2020-12-03', 'FJODMA20T03E648S',
       NULL, '3898867296',
       'Viale Carlo Albero 4', 'Codogno',
       '2025-10-27', NULL,
       'Tesseramento FIJLKAM 25/26 n. 1044707 (Tessera Promozionale)', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'FJODMA20T03E648S');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Gabriele', 'Galeone', '2015-05-14', 'GLNGRL15E14C816C',
       'galeonewilli@libero.it', '3313758095',
       'Vicolo Leoncavallo', 'Abbadia Cerreto',
       '2026-01-26', 'Arancione',
       'Tesseramento FIJLKAM 25/26 n. 1001600 (Atleta - PA)', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'GLNGRL15E14C816C');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Olivia', 'Galli', '2013-04-22', 'GLLLVO13D62C816T',
       NULL, '3883249977',
       'VIALE RISORGIMENTO, 17', 'Codogno',
       '2026-03-23', 'Arancione',
       'Tesseramento FIJLKAM 25/26 n. 791148 (Atleta - AG (Femminile))', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'GLLLVO13D62C816T');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Martino', 'Ghisalberti', '2016-10-19', 'GHSMTN16R19G535O',
       'maryspelta@gmail.com', '3282166075',
       'via martiri della libertà,14', 'Caselle Landi',
       '2026-01-26', 'Arancione',
       'Tesseramento FIJLKAM 25/26 n. 844178 (Atleta - PA)', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'GHSMTN16R19G535O');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Luca', 'Griffini', '2020-05-17', 'GRFLCU20E17G535S',
       NULL, NULL,
       'Via Polenghi 6', 'Codogno',
       '2026-02-02', 'Bianca',
       'Tesseramento FIJLKAM 25/26 n. 1076916 (Atleta - PA)', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'GRFLCU20E17G535S');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Wessal', 'Griguia', '2019-02-15', 'GRGWSL19B55E648F',
       NULL, '3914641827',
       'Via San Biagio1F', 'Comazzo',
       '2025-10-15', NULL,
       'Tesseramento FIJLKAM 25/26 n. 1040322 (Tessera Promozionale)', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'GRGWSL19B55E648F');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Youssef', 'Griguia', '2016-08-18', 'GRGYSF16M18C816N',
       NULL, '3914641827',
       'Via San Biagio1F', 'Codogno',
       '2025-11-07', 'Bianca',
       'Tesseramento FIJLKAM 25/26 n. 1048525 (Atleta - PA)', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'GRGYSF16M18C816N');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Mattia', 'Guardinceri', '2018-03-28', 'GRDMTT18C28D150X',
       NULL, NULL,
       'viale Zara 10', 'Codogno',
       '2025-11-07', 'Bianca',
       'Tesseramento FIJLKAM 25/26 n. 1048516 (Atleta - PA)', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'GRDMTT18C28D150X');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Youssef', 'Haida', '2016-12-13', 'HDAYSF16T13C816X',
       NULL, NULL,
       'Via dei Mille 2', 'Codogno',
       '2026-02-02', NULL,
       'Tesseramento FIJLKAM 25/26 n. 1076913 (Tessera Promozionale)', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'HDAYSF16T13C816X');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Fatima Azahra', 'Hiba', '2007-10-17', 'HBIFMZ07R57Z131V',
       'hibafatimaazahara07@gmail.com', '3288093639',
       'VIA CONTARDI,3', 'Codogno',
       '2026-01-26', 'Verde',
       'Tesseramento FIJLKAM 25/26 n. 908434 (Atleta - AG (Femminile))', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'HBIFMZ07R57Z131V');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Sebatian', 'Karoqja', '2016-08-22', 'KPRSST16M22F205Y',
       'serdian83@gmail.com', '3271887380',
       'via ST Lodigiana , 1', 'Somaglia',
       '2026-01-16', 'Verde',
       'Tesseramento FIJLKAM 25/26 n. 844184 (Atleta - PA)', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'KPRSST16M22F205Y');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Sirio', 'Karoqja', '2015-04-13', 'KRQSRI15D13F205Y',
       'serdian83@gmail.com', '3271887380',
       'via strada lodigiana,1', 'Somaglia',
       '2026-01-16', 'Verde',
       'Tesseramento FIJLKAM 25/26 n. 844701 (Atleta - PA)', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'KRQSRI15D13F205Y');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Maurizio', 'Losi', '2017-07-28', 'LSOMRZ17L28C816R',
       'elena.venturini@libero.it', NULL,
       'ZECCA, 4d', 'San Fiorano',
       '2026-01-16', 'Arancione',
       'Tesseramento FIJLKAM 25/26 n. 908279 (Atleta - PA)', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'LSOMRZ17L28C816R');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Elia', 'Maini', '2016-10-09', 'MNALEI16R09C816C',
       NULL, '3663221730',
       'VIALE TRANTO 1/A', 'Codogno',
       '2026-01-26', 'Arancione',
       'Tesseramento FIJLKAM 25/26 n. 791147 (Atleta - PA)', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'MNALEI16R09C816C');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Marcello', 'Mariani', '2015-02-26', 'MRNMCL15B26C816M',
       'elisa.caserini@gmail.com', NULL,
       'via Marco Polo', 'Fombio',
       '2026-01-26', 'Arancione',
       'Tesseramento FIJLKAM 25/26 n. 702280 (Atleta - PA)', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'MRNMCL15B26C816M');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Marco', 'Maris', '2016-03-26', 'MRSMRC16C26C816E',
       'elenarion@gmail.com', '3383934711',
       'Viale Resistenza', 'Codogno',
       '2026-01-26', 'Arancione',
       'Tesseramento FIJLKAM 25/26 n. 871777 (Atleta - PA)', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'MRSMRC16C26C816E');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Diego', 'Mascherpa', '1979-09-25', 'MSCDGI79P25C816S',
       'diego.mascherpa@gmail.com', '3475084547',
       'viale resistenza', 'Codogno',
       '2026-03-19', 'Gialla',
       'Tesseramento FIJLKAM 25/26 n. 1012413 (Atleta/Con disabilità NA)', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'MSCDGI79P25C816S');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Luigi', 'Mastronardo', '2000-07-25', 'MSTLGU00L25F205J',
       NULL, '3662979605',
       'Via Armando diaz 37', 'Abbadia Cerreto',
       '2026-01-26', 'Verde',
       'Tesseramento FIJLKAM 25/26 n. 973076 (Atleta Amatore)', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'MSTLGU00L25F205J');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Giorgio', 'Mezza', '2007-03-02', 'MZZGRG07C02C816L',
       'giorgiomezza07@gmail.com', '3792152261',
       'Via Zazzera', 'Codogno',
       '2026-01-09', 'Bianca',
       'Tesseramento FIJLKAM 25/26 n. 1066483 (Atleta - AG (Maschile))', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'MZZGRG07C02C816L');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Ryan', 'Molinari', '2013-02-02', 'MLNRYN13B02E648X',
       'williampara@libero.it', '3921673404',
       'Via Duca D'' Aosta Retegno', 'Fombio',
       '2026-01-26', 'Arancione',
       'Tesseramento FIJLKAM 25/26 n. 920738 (Atleta Amatore)', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'MLNRYN13B02E648X');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Maurizio', 'Origgi', '1964-10-23', 'RGGMRZ64R23I690N',
       'maurizioriggi23@gmail.com', NULL,
       'VIALE LOMBARDIA 15', 'Truccazzano',
       '2026-04-24', 'Nera (2° DAN)',
       'Tesseramento FIJLKAM 25/26 n. 1016604 (Atleta Amatore)', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'RGGMRZ64R23I690N');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Abdelkoddos', 'Ouarrak', '2017-09-06', 'RRKBLK17P06E648F',
       'maatiork@gmail.com', '3358074276',
       'Vale Cairo, 15 C', 'Codogno',
       '2026-01-12', 'Arancione',
       'Tesseramento FIJLKAM 25/26 n. 877740 (Atleta - PA)', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'RRKBLK17P06E648F');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Firdaos', 'Ouarrak', '2013-02-15', 'RRKFDS13B55E648Q',
       'maatierek@gmail.com', '3358074276',
       'Viale Cairo, 15 c', 'Codogno',
       '2026-01-12', 'Arancione',
       'Tesseramento FIJLKAM 25/26 n. 877739 (Atleta - AG (Femminile))', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'RRKFDS13B55E648Q');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Vincenzo', 'Palma', '1978-07-22', 'PLMVCN78L22C816O',
       'vincenzopalma@libero.it', '3285426422',
       'VIA LORENZO MONTI,10', 'Codogno',
       '2026-03-19', 'Blu',
       'Tesseramento FIJLKAM 25/26 n. 1000170 (Atleta/Con disabilità NA)', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'PLMVCN78L22C816O');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Greta', 'Parisi', '2008-11-12', 'PRSGRT08S52C816D',
       'gretaparisi2008@gmail.com', '3914851574',
       'via carlo lamberti', 'Codogno',
       '2026-01-14', 'Nera (1° DAN)',
       'Tesseramento FIJLKAM 25/26 n. 585565 (Atleta - AG (Femminile))', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'PRSGRT08S52C816D');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Nicholas', 'Pianu', '2016-05-04', 'PNINHL16E04F205T',
       'didifinn@gmail.com', NULL,
       'GIOVANNI PAOLO II', 'Borgo San Giovanni',
       '2025-11-07', 'Bianca',
       'Tesseramento FIJLKAM 25/26 n. 1048517 (Atleta - PA)', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'PNINHL16E04F205T');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Luca', 'Riccio', '2020-06-11', 'RCCLCU20H11G535F',
       NULL, '3397842590',
       'Via scoto 42', 'Piacenza',
       '2025-10-27', 'Bianca',
       'Tesseramento FIJLKAM 25/26 n. 1044787 (Atleta - PA)', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'RCCLCU20H11G535F');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Riccardo', 'Rossi', '2002-04-01', 'RSSRCR02D01C816U',
       'riccardo02.rossi@gmail.com', NULL,
       'VIA GULF ITALIANA 9', 'Terranova dei Passerini',
       '2026-01-31', 'Nera (2° DAN)',
       'Tesseramento FIJLKAM 25/26 n. 223480 (Atleta - AG (Maschile))', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'RSSRCR02D01C816U');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Lisa', 'Scotti', '2000-05-15', 'SCTLSI00E55C816U',
       NULL, '3319212943',
       'VIA BELLONI 7', 'Codogno',
       '2026-04-24', 'Nera (1° DAN)',
       'Tesseramento FIJLKAM 25/26 n. 223485 (Atleta Amatore)', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'SCTLSI00E55C816U');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Rassoul', 'Sow', '2021-03-23', 'SWOMMD21C23E648K',
       NULL, NULL,
       'Via del borgo 10', 'Fombio',
       '2026-01-16', 'Bianca',
       'Tesseramento FIJLKAM 25/26 n. 1070478 (Atleta - PA)', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'SWOMMD21C23E648K');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Stefano', 'Spotti', '2019-11-25', 'SPTSFN19S25G535A',
       NULL, '3920642163',
       'cascina cantonale , 1', 'Somaglia',
       '2025-10-24', 'Bianca',
       'Tesseramento FIJLKAM 25/26 n. 1043953 (Atleta - PA)', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'SPTSFN19S25G535A');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Emiliano', 'Stefanoni', '1961-10-06', 'STFMLN61R06C816T',
       'e.tasco@virgilio.it', '3355754197',
       'via Angelo Polenghi', 'Codogno',
       '2026-01-26', 'Marrone',
       'Tesseramento FIJLKAM 25/26 n. 871864 (Atleta Amatore)', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'STFMLN61R06C816T');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Andrea', 'Viccardi', '1985-09-25', 'VCCNDR85P25G535B',
       'andreaviccardi85@gmail.com', '3497019257',
       'Viale risorgimento 17', 'Codogno',
       '2026-01-26', 'Verde',
       'Tesseramento FIJLKAM 25/26 n. 1010274 (Atleta Amatore)', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'VCCNDR85P25G535B');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Enea', 'Viccardi', '2017-07-17', 'VCCNEE17L17C816F',
       'simo.bore@libero.it', '3496176830',
       'Viale Risorgimento', 'Codogno',
       '2026-01-16', 'Arancione',
       'Tesseramento FIJLKAM 25/26 n. 871727 (Atleta - PA)', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'VCCNEE17L17C816F');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Greta', 'Viccardi', '2014-10-04', 'VCCGRT14R44C816I',
       'simo.bore@libero.it', '3496176830',
       'Viale Risorgimento', 'Codogno',
       '2026-04-24', 'Arancione',
       'Tesseramento FIJLKAM 25/26 n. 871736 (Atleta - AG (Femminile))', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'VCCGRT14R44C816I');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Ivan', 'Viccardi', '2020-06-20', 'VCCVNI20H20G535W',
       NULL, '3497019257',
       'Viale risorgimento 17', 'Codogno',
       '2026-01-16', 'Gialla',
       'Tesseramento FIJLKAM 25/26 n. 975977 (Atleta - PA)', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'VCCVNI20H20G535W');

INSERT INTO atleti (nome, cognome, data_nascita, codice_fiscale, email, telefono,
                    indirizzo, citta, data_iscrizione, cintura, note, attivo)
SELECT 'Malak', 'Zomorroud', '2015-12-08', 'ZMRMLK15T48Z330R',
       'nadiamhaida8@gmail.com', '3518713022',
       'Via san Biagio', 'Codogno',
       '2025-10-08', NULL,
       'Tesseramento FIJLKAM 25/26 n. 1036785 (Tessera Promozionale)', TRUE
WHERE NOT EXISTS (SELECT 1 FROM atleti WHERE codice_fiscale = 'ZMRMLK15T48Z330R');
