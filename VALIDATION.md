# Sprint 1 Validation Results

Tested October 26, 2026 using Node.js 22 and a headless Chromium browser.

## Automated helper tests

Command: `node --test tests/*.test.js`

Result: **7 passed, 0 failed**.

| Test | Result |
| --- | --- |
| Calculate course average from graded work | Pass |
| Show N/A when nothing has been graded | Pass |
| Find the next unfinished due assignment | Pass |
| Search by course title, code, or instructor | Pass |
| Search assignments by title or status | Pass |
| Parse deep links for courses, grades, and assignments | Pass |
| Escape HTML characters in displayed content | Pass |

## Browser interaction smoke test

Result: **Pass**. Verified in headless Chromium via Playwright using in-memory HTML, CSS, and JavaScript injection because the testing sandbox disallowed direct page navigation.

| Interaction | Expected result | Actual result |
| --- | --- | --- |
| Open app | Welcome page loads | Pass |
| Select Open student dashboard | Five demo course cards appear | Pass |
| Search `graphics` | One course remains | Pass |
| Select Software Engineering | Course overview loads | Pass |
| Select Assignments | Three assignments appear | Pass |
| Search `peer` | One assignment remains | Pass |
| Select Grades & feedback | Instructor comments appear | Pass |
| Open Project Proposal | Detail page displays score and feedback | Pass |
| Navigate back | Assignments list returns | Pass |
| Select global Grades & feedback | Feedback hub displays graded work | Pass |
| Load courses at 390 px wide | Mobile layout has no horizontal overflow | Pass |

No browser JavaScript errors were reported in the smoke test.

## Evidence screenshots

- `screenshot-welcome.png`: Initial JAME LMS loading/landing screen
- `screenshot-courses.png`: Running Courses dashboard
- `screenshot-course.png`: Running Software Engineering overview
- `screenshot-mobile.png`: Responsive dashboard at 390 px wide

The screenshots show a functional browser UI, **not code**. All records are fictional demo records. The prototype has not been integrated with an actual backend, login system, or Canvas course data.
