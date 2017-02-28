package ee.adit.dvk.api.container.v1;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ee.adit.dvk.api.container.DhlEmailHeader;
import ee.adit.dvk.api.container.SaatjaDefineeritud;

public class Metainfo {
    // Meta-automatic
    private int dhlId;
    private String dhlSaabumisviis;
    private Date dhlSaabumisaeg;
    private String dhlSaatmisviis;
    private Date dhlSaatmisaeg;
    private String dhlSaatjaAsutuseNr;
    private String dhlSaatjaAsutuseNimi;
    private String dhlSaatjaIsikukood;
    private String dhlSaajaAsutuseNr;
    private String dhlSaajaAsutuseNimi;
    private String dhlSaajaIsikukood;
    private String dhlSaatjaEpost;
    private String dhlSaajaEpost;
    private DhlEmailHeader dhlEmailHeader;
    private String dhlKaust;

    // Meta-manual
    private String koostajaAsutuseNr;
    private String saajaAsutuseNr;
    private String koostajaDokumendinimi;
    private String koostajaDokumendityyp;
    private String koostajaVotmesona;
    private String koostajaDokumendinr;
    private String koostajaKuupaev;
    private String koostajaAsutuseNimi;
    private String koostajaAsutuseKontakt;
    private String autoriOsakond;
    private String autoriIsikukood;
    private String autoriNimi;
    private String autoriKontakt;
    private String seotudDhlId;
    private String seotudDokumendiNrKoostajal;
    private String seotudDokumendinrSaajal;
    private String saatjaDokumendinr;
    private String saatjaAsutuseKontakt;
    private String saajaIsikukood;
    private String saajaNimi;
    private String saajaOsakond;
    private String koostajaFailinimi;
    private Object koostajaKataloog;
    private Object koostajaKokkuvote;
    private Object sisuId;
    private Date saatjaKuupaev;
    private List<SaatjaDefineeritud> saatjaDefineeritud;


    // Meta-automatic
    public int getDhlId() {
        return dhlId;
    }

    public void setDhlId(int dhlId) {
        this.dhlId = dhlId;
    }

    public String getDhlSaabumisviis() {
        return dhlSaabumisviis;
    }

    public void setDhlSaabumisviis(String dhlSaabumisviis) {
        this.dhlSaabumisviis = dhlSaabumisviis;
    }

    public Date getDhlSaabumisaeg() {
        return dhlSaabumisaeg;
    }

    public void setDhlSaabumisaeg(Date dhlSaabumisaeg) {
        this.dhlSaabumisaeg = dhlSaabumisaeg;
    }

    public String getDhlSaatmisviis() {
        return dhlSaatmisviis;
    }

    public void setDhlSaatmisviis(String dhlSaatmisviis) {
        this.dhlSaatmisviis = dhlSaatmisviis;
    }

    public Date getDhlSaatmisaeg() {
        return dhlSaatmisaeg;
    }

    public void setDhlSaatmisaeg(Date dhlSaatmisaeg) {
        this.dhlSaatmisaeg = dhlSaatmisaeg;
    }

    public String getDhlSaatjaAsutuseNr() {
        return dhlSaatjaAsutuseNr;
    }

    public void setDhlSaatjaAsutuseNr(String dhlSaatjaAsutuseNr) {
        this.dhlSaatjaAsutuseNr = dhlSaatjaAsutuseNr;
    }

    public String getDhlSaatjaAsutuseNimi() {
        return dhlSaatjaAsutuseNimi;
    }

    public void setDhlSaatjaAsutuseNimi(String dhlSaatjaAsutuseNimi) {
        this.dhlSaatjaAsutuseNimi = dhlSaatjaAsutuseNimi;
    }

    public String getDhlSaatjaIsikukood() {
        return dhlSaatjaIsikukood;
    }

    public void setDhlSaatjaIsikukood(String dhlSaatjaIsikukood) {
        this.dhlSaatjaIsikukood = dhlSaatjaIsikukood;
    }

    public String getDhlSaajaAsutuseNr() {
        return dhlSaajaAsutuseNr;
    }

    public void setDhlSaajaAsutuseNr(String dhlSaajaAsutuseNr) {
        this.dhlSaajaAsutuseNr = dhlSaajaAsutuseNr;
    }

    public String getDhlSaajaAsutuseNimi() {
        return dhlSaajaAsutuseNimi;
    }

    public void setDhlSaajaAsutuseNimi(String dhlSaajaAsutuseNimi) {
        this.dhlSaajaAsutuseNimi = dhlSaajaAsutuseNimi;
    }

    public String getDhlSaajaIsikukood() {
        return dhlSaajaIsikukood;
    }

    public void setDhlSaajaIsikukood(String dhlSaajaIsikukood) {
        this.dhlSaajaIsikukood = dhlSaajaIsikukood;
    }

    public String getDhlSaatjaEpost() {
        return dhlSaatjaEpost;
    }

    public void setDhlSaatjaEpost(String dhlSaatjaEpost) {
        this.dhlSaatjaEpost = dhlSaatjaEpost;
    }

    public String getDhlSaajaEpost() {
        return dhlSaajaEpost;
    }

    public void setDhlSaajaEpost(String dhlSaajaEpost) {
        this.dhlSaajaEpost = dhlSaajaEpost;
    }

    public DhlEmailHeader getDhlEmailHeader() {
        return dhlEmailHeader;
    }

    public void setDhlEmailHeader(DhlEmailHeader dhlEmailHeader) {
        this.dhlEmailHeader = dhlEmailHeader;
    }

    public String getDhlKaust() {
        return dhlKaust;
    }

    public void setDhlKaust(String dhlKaust) {
        this.dhlKaust = dhlKaust;
    }


    // Meta-manual
    public String getKoostajaAsutuseNr() {
        return koostajaAsutuseNr;
    }

    public void setKoostajaAsutuseNr(String koostajaAsutuseNr) {
        this.koostajaAsutuseNr = koostajaAsutuseNr;
    }

    public String getSaajaAsutuseNr() {
        return saajaAsutuseNr;
    }

    public void setSaajaAsutuseNr(String saajaAsutuseNr) {
        this.saajaAsutuseNr = saajaAsutuseNr;
    }

    public String getKoostajaDokumendinimi() {
        return koostajaDokumendinimi;
    }

    public void setKoostajaDokumendinimi(String koostajaDokumendinimi) {
        this.koostajaDokumendinimi = koostajaDokumendinimi;
    }

    public String getKoostajaDokumendityyp() {
        return koostajaDokumendityyp;
    }

    public void setKoostajaDokumendityyp(String koostajaDokumendityyp) {
        this.koostajaDokumendityyp = koostajaDokumendityyp;
    }

    public String getKoostajaVotmesona() {
        return koostajaVotmesona;
    }

    public void setKoostajaVotmesona(String koostajaVotmesona) {
        this.koostajaVotmesona = koostajaVotmesona;
    }

    public String getKoostajaDokumendinr() {
        return koostajaDokumendinr;
    }

    public void setKoostajaDokumendinr(String koostajaDokumendinr) {
        this.koostajaDokumendinr = koostajaDokumendinr;
    }

    public String getKoostajaKuupaev() {
        return koostajaKuupaev;
    }

    public void setKoostajaKuupaev(String koostajaKuupaev) {
        this.koostajaKuupaev = koostajaKuupaev;
    }

    public String getKoostajaAsutuseNimi() {
        return koostajaAsutuseNimi;
    }

    public void setKoostajaAsutuseNimi(String koostajaAsutuseNimi) {
        this.koostajaAsutuseNimi = koostajaAsutuseNimi;
    }

    public String getKoostajaAsutuseKontakt() {
        return koostajaAsutuseKontakt;
    }

    public void setKoostajaAsutuseKontakt(String koostajaAsutuseKontakt) {
        this.koostajaAsutuseKontakt = koostajaAsutuseKontakt;
    }

    public String getAutoriOsakond() {
        return autoriOsakond;
    }

    public void setAutoriOsakond(String autoriOsakond) {
        this.autoriOsakond = autoriOsakond;
    }

    public String getAutoriIsikukood() {
        return autoriIsikukood;
    }

    public void setAutoriIsikukood(String autoriIsikukood) {
        this.autoriIsikukood = autoriIsikukood;
    }

    public String getAutoriNimi() {
        return autoriNimi;
    }

    public void setAutoriNimi(String autoriNimi) {
        this.autoriNimi = autoriNimi;
    }

    public String getAutoriKontakt() {
        return autoriKontakt;
    }

    public void setAutoriKontakt(String autoriKontakt) {
        this.autoriKontakt = autoriKontakt;
    }

    public String getSeotudDhlId() {
        return seotudDhlId;
    }

    public void setSeotudDhlId(String seotudDhlId) {
        this.seotudDhlId = seotudDhlId;
    }

    public String getSeotudDokumendiNrKoostajal() {
        return seotudDokumendiNrKoostajal;
    }

    public void setSeotudDokumendiNrKoostajal(String seotudDokumendiNrKoostajal) {
        this.seotudDokumendiNrKoostajal = seotudDokumendiNrKoostajal;
    }

    public String getSeotudDokumendinrSaajal() {
        return seotudDokumendinrSaajal;
    }

    public void setSeotudDokumendinrSaajal(String seotudDokumendinrSaajal) {
        this.seotudDokumendinrSaajal = seotudDokumendinrSaajal;
    }

    public String getSaatjaDokumendinr() {
        return saatjaDokumendinr;
    }

    public void setSaatjaDokumendinr(String saatjaDokumendinr) {
        this.saatjaDokumendinr = saatjaDokumendinr;
    }

    public String getSaatjaAsutuseKontakt() {
        return saatjaAsutuseKontakt;
    }

    public void setSaatjaAsutuseKontakt(String saatjaAsutuseKontakt) {
        this.saatjaAsutuseKontakt = saatjaAsutuseKontakt;
    }

    public String getSaajaIsikukood() {
        return saajaIsikukood;
    }

    public void setSaajaIsikukood(String saajaIsikukood) {
        this.saajaIsikukood = saajaIsikukood;
    }

    public String getSaajaNimi() {
        return saajaNimi;
    }

    public void setSaajaNimi(String saajaNimi) {
        this.saajaNimi = saajaNimi;
    }

    public String getSaajaOsakond() {
        return saajaOsakond;
    }

    public void setSaajaOsakond(String saajaOsakond) {
        this.saajaOsakond = saajaOsakond;
    }

    public String getKoostajaFailinimi() {
        return koostajaFailinimi;
    }

    public void setKoostajaFailinimi(String koostajaFailinimi) {
        this.koostajaFailinimi = koostajaFailinimi;
    }

    public Object getKoostajaKataloog() {
        return koostajaKataloog;
    }

    public void setKoostajaKataloog(Object koostajaKataloog) {
        this.koostajaKataloog = koostajaKataloog;
    }

    public Object getKoostajaKokkuvote() {
        return koostajaKokkuvote;
    }

    public void setKoostajaKokkuvote(Object koostajaKokkuvote) {
        this.koostajaKokkuvote = koostajaKokkuvote;
    }

    public Object getSisuId() {
        return sisuId;
    }

    public void setSisuId(Object sisuId) {
        this.sisuId = sisuId;
    }

    public Date getSaatjaKuupaev() {
        return saatjaKuupaev;
    }

    public void setSaatjaKuupaev(Date saatjaKuupaev) {
        this.saatjaKuupaev = saatjaKuupaev;
    }

    public List<SaatjaDefineeritud> getSaatjaDefineeritud() {
        return saatjaDefineeritud;
    }

    public void setSaatjaDefineeritud(List<SaatjaDefineeritud> saatjaDefineeritud) {
        this.saatjaDefineeritud = saatjaDefineeritud;
    }

    public void createList(boolean saatjaDefineeritud) {
        if (saatjaDefineeritud) {
            if (this.saatjaDefineeritud == null) {
                this.saatjaDefineeritud = new ArrayList<SaatjaDefineeritud>();
            }
        }
    }
}
