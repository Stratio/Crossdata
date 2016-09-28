Feature: [CROSSDATA-677] Microstrategy - Tests Queries

  Scenario: [CROSSDATA-624] MICRO_JDBC_QUERY_1
    When I execute 'select a11.COD_DATIMP COD_BANCSB,max(a11.COD_OFIPALCL) COD_OFICI,max(a11.DES_OFIPA) DES_OFIPA,max(a11.COD_BANCSBM) COD_BANCSB0,a11.COD_IDFISCAK COD_IDFISCAK,a11.DES_NOCLIE DES_NOCLIE,a11.COD_PERSOCL COD_PERSOCL,a11.COD_SEMAMES COD_SEMAMES,a11.DES_DTMM DES_DTMM,a11.DES_ZOMM DES_ZOMM,a11.DES_CBCMM DES_CBCMM,a11.DES_OFIMM DES_OFIMM,a11.DES_UNIMMDV DES_UNIMMDV,a11.DES_NIVMM DES_NIVMM,a11.DES_EVENMDV DES_EVENMDV,a11.DES_SUBSEGLO DES_SEGCLM,a11.COD_CONTIGO COD_CONTIGO,max(a12.DES_CONTIGO) DES_CONTIGO,max(a12.COD_ORDCONTI) COD_ORDCONTI,sum(a11.COD_COMEX) WJXBFS1,sum(a11.COD_EMIRECIB) WJXBFS2,sum(a11.COD_INGRESCL) WJXBFS3,sum(a11.COD_MAILCL) WJXBFS4,sum(a11.COD_MOVIL) WJXBFS5,sum(a11.COD_NETCL) WJXBFS6,sum(a11.IMP_OBJPROP) WJXBFS7,sum(a11.COD_RECIBCL) WJXBFS8,sum(a11.COD_SEGUROS) WJXBFS9,sum(to_number(a11.COD_TARJECL)) WJXBFSa,sum(a11.COD_TPVCL) WJXBFSb,sum(a11.IMP_TRANFIN) WJXBFSc,sum(a11.IMP_TRANINI) WJXBFSd,sum(a11.IMP_VENTCRUZ) WJXBFSe from KUYA.TKUYAKCL a11 join KUYA.TKUYAKGO a12 on (a11.COD_CONTIGO = a12.COD_CONTIGO) where (a11.COD_SEMAMES in ('8.20a26JUN') and ((a11.COD_BANCSBM = '0182' and (a11.DES_OFIPA like '%0013%' or a11.COD_OFIPALCL = '0013')) or  (a11.COD_BANCSBM = '0182' and (a11.DES_OFIPA like '%6051%' or a11.COD_OFIPALCL = '6051')))) group by a11.COD_DATIMP,a11.COD_IDFISCAK,a11.DES_NOCLIE,a11.COD_PERSOCL,a11.COD_SEMAMES,a11.DES_DTMM,a11.DES_ZOMM,a11.DES_CBCMM,a11.DES_OFIMM,a11.DES_UNIMMDV,a11.DES_NIVMM,a11.DES_EVENMDV,a11.DES_SUBSEGLO,a11.COD_CONTIGO'
    Then an exception 'IS NOT' thrown
    #Then The result has to have '1' rows
    #  |COD_BANCSB-string|COD_OFICI-string|DES_OFIPA-string|COD_BANCSB0-string|COD_IDFISCAK-string|DES_NOCLIE-string|COD_PERSOCL-string|COD_SEMAMES-string|DES_DTMM-string|DES_ZOMM-string|DES_CBCMM-string|DES_OFIMM-string|DES_UNIMMDV-string|DES_NIVMM-string|DES_EVENMDV-string|DES_SEGCLM-string|COD_CONTIGO-integer|DES_CONTIGO-string|COD_ORDCONTI-integer|WJXBFS1-long|WJXBFS2-long|WJXBFS3-long|WJXBFS4-long|WJXBFS5-long|WJXBFS6-long|WJXBFS7-long|WJXBFS8-long|WJXBFS9-long|WJXBFSa-double|WJXBFSb-long|WJXBFSc-long|WJXBFSd-long|WJXBFSe-long|

  Scenario: [CROSSDATA-625, CROSSDATA-645] MICRO_JDBC_QUERY_2
    When I execute 'create table ZZMQ00 nologging as select a11.COD_CONTIGO  COD_CONTIGO,a11.DES_NIVMM  DES_NIVMM,a11.DES_OFIMM  DES_OFIMM,a11.DES_UNIREF  DES_UNIREF,a11.DES_UNIMMDV  DES_UNIMMDV,a11.DES_EVENMDV  DES_EVENMDV,a11.DES_EVENCORT  DES_EVENCORT,a11.DES_SEGCLM  DES_SEGCLM,a11.COD_SEMAMES  COD_SEMAMES from KUYA.TKUYAKSE a11 where ((a11.COD_BANCSBM = '0182' and (a11.DES_OFIPA like '%0013%' or a11.COD_OFIMM = '0013')) or (a11.COD_BANCSBM = '0182' and (a11.DES_OFIPA like '%6051%' or a11.COD_OFIMM = '6051'))) and((a11.IMP_CUMPLIMI <> 0.0 or a11.QNU_BASEVMDV <> 0.0 or a11.IMP_BASTRANS <> 0.0 or a11.IMP_BATRAINI <> 0.0 or a11.IMP_BASINCRE <> 0.0))'
    Then an exception 'IS NOT' thrown
    When I execute 'select a11.DES_BANMM  DES_BANMM,a11.DES_DTMM  DES_DTMM,a11.DES_ZOMM  DES_ZOMM,a11.DES_CBCMM  DES_CBCMM,a11.DES_OFIMM  DES_OFIMM,a11.DES_UNIMMDV  DES_UNIMMDV,a11.DES_NIVMM  DES_NIVMM,a11.COD_DATIMP  COD_BANCSB,a11.COD_OFIMM  COD_OFICI,a11.DES_OFIPA  DES_OFIPA,a11.COD_BANCSBM  COD_BANCSB0,a11.COD_SEMAMES  COD_SEMAMES,a11.DES_EVENCORT  DES_EVENCORT,a11.DES_UNIREF  DES_UNIREF,a11.DES_EVENMDV  DES_EVENMDV,a11.DES_SEGCLM  DES_SEGCLM,a11.COD_CONTIGO  COD_CONTIGO,a13.DES_CONTIGO  DES_CONTIGO,a13.COD_ORDCONTI  COD_ORDCONTI,a11.IMP_CUMPLIMI  WJXBFS1,a11.IMP_BASTRANS  WJXBFS2,a11.QNU_BASEVMDV  WJXBFS3,a11.IMP_BATRAINI  WJXBFS4,a11.IMP_BASINCRE  WJXBFS5 from KUYA.TKUYAKSE a11 join ZZMQ00 pa12 on (a11.COD_CONTIGO = pa12.COD_CONTIGO and a11.COD_SEMAMES = pa12.COD_SEMAMES and a11.DES_EVENCORT = pa12.DES_EVENCORT and a11.DES_EVENMDV = pa12.DES_EVENMDV and a11.DES_NIVMM = pa12.DES_NIVMM and a11.DES_OFIMM = pa12.DES_OFIMM and a11.DES_SEGCLM = pa12.DES_SEGCLM and a11.DES_UNIMMDV = pa12.DES_UNIMMDV and a11.DES_UNIREF = pa12.DES_UNIREF)join KUYA.TKUYAKGO a13 on(a11.COD_CONTIGO = a13.COD_CONTIGO) where((a11.COD_BANCSBM = '0182' and (a11.DES_OFIPA like '%0013%' or a11.COD_OFIMM = '0013')) or (a11.COD_BANCSBM = '0182' and (a11.DES_OFIPA like '%6051%' or a11.COD_OFIMM = '6051')))'
    Then an exception 'IS NOT' thrown

  Scenario: [CROSSDATA-626] MICRO_JDBC_QUERY_3
    When I execute 'select a11.DES_BANMM DES_BANMM,a11.DES_DTMM DES_DTMM,a11.DES_ZOMM DES_ZOMM,a11.DES_CBCMM DES_CBCMM,a11.DES_OFIMM DES_OFIMM,a11.DES_UNIMMDV DES_UNIMMDV,a11.DES_NIVMM DES_NIVMM,a11.COD_DATIMP COD_BANCSB,a11.COD_OFIMM COD_OFICI,a11.DES_OFIPA DES_OFIPA,a11.COD_BANCSBM COD_BANCSB0,a11.COD_SEMAMES COD_SEMAMES,a11.DES_EVENCORT DES_EVENCORT,a11.DES_UNIREF DES_UNIREF,a11.DES_EVENMDV DES_EVENMDV,a11.DES_SEGCLM DES_SEGCLM,a11.COD_CONTIGO COD_CONTIGO,a12.DES_CONTIGO DES_CONTIGO,a12.COD_ORDCONTI COD_ORDCONTI,a11.IMP_BASINCRE WJXBFS1,a11.QNU_BASEVMDV WJXBFS2,a11.IMP_BATRAINI WJXBFS3,a11.IMP_BASTRANS WJXBFS4,a11.IMP_CUMPLIMI WJXBFS5,a11.IMP_OBJPROP WJXBFS6,a11.QNU_INCRETO WJXBFS7,a11.QNU_RANKMAX WJXBFS8,a11.QNU_RAKNICON WJXBFS9,a11.QNU_RAKEVMDV WJXBFSa,a11.QNU_RAKTRANS WJXBFSb,a11.IMP_RECUMPLI WJXBFSc,a11.IMP_REINCRE WJXBFSd,a11.QNU_RENUMMDV WJXBFSe,a11.IMP_RETRANSA WJXBFSf,a11.IMP_RETRAINI WJXBFS10 from KUYA.TKUYAKSE a11 join KUYA.TKUYAKGO a12 on (a11.COD_CONTIGO = a12.COD_CONTIGO) where (a11.COD_SEMAMES in ('8.20a26JUN') and ((a11.COD_BANCSBM = '0182' and (a11.DES_OFIPA like '%0013%' or a11.COD_OFIMM = '0013')) or (a11.COD_BANCSBM = '0182' and (a11.DES_OFIPA like '%6051%' or a11.COD_OFIMM = '6051'))))'
    Then an exception 'IS NOT' thrown

  Scenario: [CROSSDATA-627] MICRO_JDBC_QUERY_4
    When I execute 'select a11.DES_BANMM DES_BANMM,a11.DES_DTMM DES_DTMM,a11.DES_ZOMM DES_ZOMM,a11.DES_CBCMM DES_CBCMM,a11.DES_OFIMM DES_OFIMM,a11.DES_UNIMMDV DES_UNIMMDV,a11.DES_NIVMM DES_NIVMM,a11.COD_DATIMP COD_BANCSB,max(a11.COD_OFIMM) COD_OFICI,max(a11.DES_OFIPA) DES_OFIPA,max(a11.COD_BANCSBM) COD_BANCSB0,a11.COD_SEMAMES COD_SEMAMES,a11.DES_EVENMDV DES_EVENMDV,a11.DES_SEGCLM DES_SEGCLM,a11.COD_CONTIGO COD_CONTIGO,max(a12.DES_CONTIGO) DES_CONTIGO,max(a12.COD_ORDCONTI) COD_ORDCONTI,sum(a11.IMP_BASINCRE) WJXBFS1,sum(a11.IMP_CUMPLIMI) WJXBFS2,sum(a11.QNU_BASEVMDV) WJXBFS3,sum(a11.IMP_BATRAINI) WJXBFS4,sum(a11.IMP_RETRANSA) WJXBFS5,sum(a11.IMP_REINCRE) WJXBFS6,sum(a11.IMP_RETRANSA) WJXBFS7 from KUYA.TKUYAKEV a11 join KUYA.TKUYAKGO a12 on (a11.COD_CONTIGO = a12.COD_CONTIGO) where (a11.COD_SEMAMES in('8.20a26JUN') and ((a11.COD_BANCSBM = '0182' and (a11.DES_OFIPA like '%0013%' or a11.COD_OFIMM = '0013')) or (a11.COD_BANCSBM = '0182' and (a11.DES_OFIPA like '%6051%' or a11.COD_OFIMM = '6051')))) group by a11.DES_BANMM,a11.DES_DTMM,a11.DES_ZOMM,a11.DES_CBCMM,a11.DES_OFIMM,a11.DES_UNIMMDV,a11.DES_NIVMM,a11.COD_DATIMP,a11.COD_SEMAMES,a11.DES_EVENMDV,a11.DES_SEGCLM,a11.COD_CONTIGO'
    Then an exception 'IS NOT' thrown

  Scenario: [CROSSDATA-627] MICRO_JDBC_QUERY_4
    When I execute 'select a11.DES_PRODMDV DES_PRODMDV,a11.COD_SEMAMES COD_SEMAMES,a11.DES_DTMM DES_DTMM,a11.DES_CBCMM DES_CBCMM,a11.DES_ZOMM DES_ZOMM,a11.DES_OFIMM DES_OFIMM,a11.DES_NIVMM DES_NIVMM,a11.COD_DATIMP COD_BANCSB,max(a11.COD_OFIMM) COD_OFICI,max(a11.DES_OFIPA) DES_OFIPA,max(a11.COD_BANCSBM) COD_BANCSB0,a11.DES_UNIMMDV DES_UNIMMDV,a11.DES_EVENMDV DES_EVENMDV,a11.COD_CONTIGO COD_CONTIGO,max(a12.DES_CONTIGO) DES_CONTIGO,max(a12.COD_ORDCONTI) COD_ORDCONTI,sum(a11.IMP_BASINCRE) WJXBFS1,sum(a11.IMP_BASTRANS) WJXBFS2,sum(a11.IMP_REINCRE) WJXBFS3,sum(a11.IMP_RETRANSA) WJXBFS4,sum(a11.QNU_TOPRODUC) WJXBFS5 from KUYA.TKUYAKPR a11 join KUYA.TKUYAKGO a12 on (a11.COD_CONTIGO = a12.COD_CONTIGO) where (a11.COD_SEMAMES in ('8.20a26JUN') and ((a11.COD_BANCSBM = '0182' and (a11.DES_OFIPA like '%0013%' or a11.COD_OFIMM = '0013')) or (a11.COD_BANCSBM = '0182' and (a11.DES_OFIPA like '%6051%' or a11.COD_OFIMM = '6051')))) group by a11.DES_PRODMDV,a11.COD_SEMAMES,a11.DES_DTMM,a11.DES_CBCMM,a11.DES_ZOMM,a11.DES_OFIMM,a11.DES_NIVMM,a11.COD_DATIMP,a11.DES_UNIMMDV,a11.DES_EVENMDV,a11.COD_CONTIGO'
    Then an exception 'IS NOT' thrown






