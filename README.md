# FINT Betaling

## Configuration
### Mandatory properties
| Key                                 | Description | Default value |
|-------------------------------------|-------------|---------------|
| fint.betaling.org-id                |             |               |
| fint.betaling.endpoints.environment |             |               |
| fint.oauth.username                 |             |               |
| fint.oauth.password                 |             |               |
| fint.oauth.client-id                |             |               |
| fint.oauth.client-secret            |             |               |
| fint.database.url                   |             |               |
| fint.database.username              |             |               |
| fint.database.password              |             |               |


### Optional properties
| Key                                           | Description                                            | Default value                                                                              |
|-----------------------------------------------|--------------------------------------------------------|--------------------------------------------------------------------------------------------|
| fint.oauth.enabled                            |                                                        | true                                                                                       |
| fint.oauth.access-token-uri                   |                                                        | https://idp.felleskomponent.no/nidp/oauth/nam/token                                        |
| fint.oauth.scope                              |                                                        | fint-client                                                                                |
| fint.betaling.principal-matching-strategy     | How to matching pricipal (fakturautsteder) with school | default                                                                                    |
| fint.betaling.endpoints.principal             |                                                        | /okonomi/faktura/fakturautsteder                                                           |
| fint.betaling.endpoints.lineitem              |                                                        | /okonomi/kodeverk/vare                                                                     | 
| fint.betaling.endpoints.organisation          |                                                        | /administrasjon/organisasjon/organisasjonselement                                          |
| fint.betaling.endpoints.school                |                                                        | /utdanning/utdanningsprogram/skole                                                         |
| fint.betaling.endpoints.basis-group           |                                                        | /utdanning/elev/basisgruppe                                                                | 
| fint.betaling.endpoints.teaching-group        |                                                        | /utdanning/timeplan/undervisningsgruppe                                                    | 
| fint.betaling.endpoints.contact-teacher-group |                                                        | /utdanning/elev/kontaktlarergruppe                                                         | 
| fint.betaling.endpoints.student-relation      |                                                        | /utdanning/elev/elevforhold                                                                | 
| fint.betaling.endpoints.person                |                                                        | /utdanning/elev/person                                                                     | 
| fint.betaling.endpoints.mva-code              |                                                        | /okonomi/kodeverk/merverdiavgift                                                           | 
| fint.betaling.endpoints.url-template          |                                                        | https://%s.felleskomponent.no%s                                                            | 
| fint.betaling.endpoints.school-resource       |                                                        | /utdanning/elev/skoleressurs                                                               | 
| fint.betaling.endpoints.employee              |                                                        | /administrasjon/personal/person                                                            | 
| fint.betaling.endpoints.invoice               |                                                        | /okonomi/faktura/faktura                                                                   | 
| fint.betaling.dnd.file-types                  |                                                        | application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/vnd.ms-excel | 
| fint.betaling.dnd.VIS-ID                      |                                                        | VIS-ID                                                                                     | 
| ~~fint.betaling.date-range~~                  |                                                        |                                                                                            |

### Invoice status

| Status       | Message                                  | Description                                                      |
|--------------|------------------------------------------|------------------------------------------------------------------|
| STORED       | Lagret, ikke fakturert                   | Lagret i backend, men ikke overført til FINT.                    |
| SENT         | Sendt til økonomisystem                  | POST'et til FINT med retur 202 Accepted.                         |
| ACCEPTED     | Klar til fakturering                     | Async overføring vellykket. Vellykket retur med location-header. |
| ISSUED       | Fakturert                                | Fakturagrunnlag er fakturert i økonomisystem.                    |
| PAID         | Betalt                                   | Finnes faktura som er betalt eller kreditert.                    |
| ERROR        | Ukjent feil                              | Ubrukt.                                                          |
| SEND_ERROR   | Feil ved innsendelse                     | Overføring til FINT har feilet.                                  |
| ACCEPT_ERROR | Feil ved oversending til økonomisystemet | FINT returnerer 4'**'-feil på overføring.                        |
| UPDATE_ERROR | Feil ved oppdatering                     | Oppdatering av fakturastatus returnerte feil.                    |
| CANCELLED    | Kansellert før fakturering               | Brukeren har slett ordren før den ble sendt til regnskap.        |
