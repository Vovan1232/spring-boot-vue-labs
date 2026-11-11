package ua.chytalnia.articleai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SaveArticleToEntity(
    String titleOfTheArticle,
    String authorSet,
    String yearOfPublication,
    String nameJournal,
    String volume,
    String issue,
    String part,
    String firstPage,
    String lastPage,
    String totalVolumeOfPrintedSheets,
    String articleNumber,
    String language,
    String publishedInUkraine,
    String publishedAbroad,
    String countryOfPublication,
    String cityOfPublication,
    String publisher,
    String doi,
    String issn,
    String eissn,
    String isbn,
    String publicationUrl,
    String repositoryUrl,
    String publicationDate,
    String publicationType,
    String specialized,
    String affiliation,
    String orcid,
    String abstractText,
    String keywords,
    String udc,
    String funding,
    String license,
    String conferenceName,
    String conferenceLocation,
    String conferenceDate,
    String edition
) {
    public SaveArticleToEntity {
        titleOfTheArticle = normalize(titleOfTheArticle, "");
        authorSet = normalize(authorSet, "");
        yearOfPublication = normalize(yearOfPublication, "0");
        nameJournal = normalize(nameJournal, "");
        volume = normalize(volume, "");
        issue = normalize(issue, "");
        part = normalize(part, "");
        firstPage = normalize(firstPage, "0");
        lastPage = normalize(lastPage, "0");
        totalVolumeOfPrintedSheets = normalize(totalVolumeOfPrintedSheets, "0");
        articleNumber = normalize(articleNumber, "");
        language = normalize(language, "");
        publishedInUkraine = normalize(publishedInUkraine, "0");
        publishedAbroad = normalize(publishedAbroad, "0");
        countryOfPublication = normalize(countryOfPublication, "");
        cityOfPublication = normalize(cityOfPublication, "");
        publisher = normalize(publisher, "");
        doi = normalize(doi, "");
        issn = normalize(issn, "");
        eissn = normalize(eissn, "");
        isbn = normalize(isbn, "");
        publicationUrl = normalize(publicationUrl, "");
        repositoryUrl = normalize(repositoryUrl, "");
        publicationDate = normalize(publicationDate, "");
        publicationType = normalize(publicationType, "");
        specialized = "Category A";
        affiliation = normalize(affiliation, "");
        orcid = normalize(orcid, "");
        abstractText = normalize(abstractText, "");
        keywords = normalize(keywords, "");
        udc = normalize(udc, "");
        funding = normalize(funding, "");
        license = normalize(license, "");
        conferenceName = normalize(conferenceName, "");
        conferenceLocation = normalize(conferenceLocation, "");
        conferenceDate = normalize(conferenceDate, "");
        edition = normalize(edition, "");
    }

    private static String normalize(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.strip();
    }
}
