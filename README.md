# FINT Betaling

## Configuration
### Mandatory properties
| Key | Description | Default value |
| ---- | ---- | ---- |
| fint.betaling.org-id |  | |
| fint.betaling.endpoints.environment | | |

### Optional properties
| Key | Description | Default value |
| ---- | ---- | ---- |
| fint.betaling.endpoints.principal | | /okonomi/faktura/fakturautsteder |
| fint.betaling.endpoints.lineitem | | /okonomi/kodeverk/vare | 
| fint.betaling.endpoints.organisation | | /administrasjon/organisasjon/organisasjonselement |
| fint.betaling.endpoints.school | | /utdanning/utdanningsprogram/skole |
| fint.betaling.endpoints.basis-group | |/utdanning/elev/basisgruppe | 
| fint.betaling.endpoints.teaching-group | |/utdanning/timeplan/undervisningsgruppe | 
| fint.betaling.endpoints.contact-teacher-group | |/utdanning/elev/kontaktlarergruppe | 
| fint.betaling.endpoints.student-relation | |/utdanning/elev/elevforhold | 
| fint.betaling.endpoints.person | |/utdanning/elev/person | 
| fint.betaling.endpoints.mva-code | |/okonomi/kodeverk/merverdiavgift | 
| fint.betaling.endpoints.url-template | |https://%s.felleskomponent.no%s | 
| fint.betaling.endpoints.school-resource | |/utdanning/elev/skoleressurs | 
| fint.betaling.endpoints.employee | |/administrasjon/personal/person | 
| fint.betaling.endpoints.invoice | |/okonomi/faktura/faktura | 
| fint.betaling.dnd.file-types | |application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/vnd.ms-excel | 
| fint.betaling.dnd.VIS-ID | |VIS-ID | 
| ~~fint.betaling.date-range~~|  | |

