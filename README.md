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

### Request limits and timeouts

Concurrency limits are shared across simultaneous jobs in each backend instance.
Paid claims are excluded from scheduled, manual, and direct status refreshes.
Sending holds its slot until acceptance or failure has been saved; refreshing holds
its slot through the invoice lookups and polling. Waiting for a slot does not block a thread.

| Property | Default | Purpose |
|----------|---------|---------|
| `fint.betaling.claim-send-max-concurrency` | `50` | Total sends awaiting acceptance per instance. |
| `fint.betaling.claim-update-max-concurrency` | `20` | Total scheduled and manual status refreshes per instance. |
| `fint.client.max-connections` | `100` | HTTP connections per remote host. |
| `fint.client.pending-acquire-max-count` | `200` | Maximum requests waiting for a connection. |
| `fint.client.connect-timeout` | `10s` | Time allowed to establish a connection. |
| `fint.client.response-timeout` | `55s` | HTTP response timeout, below the gateway cutoff. |
| `fint.client.pending-acquire-timeout` | `15s` | Time allowed to wait for an HTTP connection. |
| `fint.client.max-idle-time` | `20s` | Idle connection lifetime; idle connections are periodically evicted. |

HTTP pool metrics are exposed through the existing Actuator/Micrometer setup.
Transient network failures and HTTP 429/502/503/504 responses are retried during
acceptance polling. Live invoice lookups also retry each failed GET at most twice,
with 1- and 2-second backoffs, while retaining the same status URL. The existing
2-minute live-read deadline includes retries and polling. After retry exhaustion,
the original error is saved as `UPDATE_ERROR`. Order POSTs and business rejections
are not automatically retried.
Cold resource caches load once across concurrent readers, remember successful empty
results, and retain existing data on a failed refresh. Failed cold loads wait at
least 30 seconds before another request triggers a retry; scheduled refreshes can
retry immediately.

### Invoice status

| Status       | Icon                     | Description                                                      |
|--------------|--------------------------|------------------------------------------------------------------|
| STORED       | Ikke sendt               | Lagret i backend, men ikke overført til FINT.                    |
| SENT         | Sendt                    | POST'et til FINT med retur 202 Accepted.                         |
| ACCEPTED     | Klar                     | Async overføring vellykket. Vellykket retur med location-header. |
| ISSUED       | Fakturert                | Fakturagrunnlag er fakturert i økonomisystem.                    |
| PAID         | Betalt                   | Finnes faktura som er betalt eller kreditert.                    |
| CREDITED     | Kreditert                | Kreditert faktura.                                               |
| ERROR        | Generell feil            | Ubrukt.                                                          |
| SEND_ERROR   | Feil ved sending         | Overføring til FINT har feilet.                                  |
| ACCEPT_ERROR | Feil fra økonomisystemet | FINT returnerer 4'**'-feil på overføring.                        |
| UPDATE_ERROR | Oppdateringsfeil         | Oppdatering av fakturastatus returnerte feil.                    |
| CANCELLED    | Kansellerte              | Brukeren har slett ordren før den ble sendt til regnskap.        |
