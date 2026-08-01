# CrewWiki production JDBC encoding recovery

- Date: 2026-07-25 (Asia/Seoul)
- Root cause: invalid characterEncoding value in PROD_DB_URL
- Correction: characterEncoding=UTF-8
- Database schema: crewwiki_db
- Safety snapshot: crewwiki-prod-pre-recovery-20260725
- Application code changes: none
