const test = require('node:test');
const assert = require('node:assert/strict');
const U = require('../utils.js');
const { courses } = require('../data.js');

test('course average includes only graded assignments', () => {
  const course = courses.find(c => c.id === 'cs3411');
  assert.equal(U.average(course), 93); // 185 / 200 rounded
});

test('course average is null when no items have scores', () => {
  assert.equal(U.average({ assignments: [{ score: null, points: 20 }] }), null);
});

test('upcoming assignment ignores graded items and chooses earliest future due date', () => {
  const course = courses.find(c => c.id === 'cs3411');
  assert.equal(U.upcoming(course, '2026-10-26').id, 'prog3');
  assert.equal(U.upcoming(course, '2026-11-10'), null);
});

test('search finds courses by code, title or instructor and ignores case', () => {
  assert.equal(U.filterCourses(courses, 'cs3141').length, 1);
  assert.equal(U.filterCourses(courses, 'GRAPHICS').length, 1);
  assert.equal(U.filterCourses(courses, 'instructor e').length, 1);
  assert.equal(U.filterCourses(courses, '  ').length, courses.length);
});

test('assignment search checks title and status', () => {
  assert.equal(U.filterAssignments(courses[0].assignments, 'peer').length, 1);
  assert.equal(U.filterAssignments(courses[0].assignments, 'graded').length, 1);
});

test('deep links route to relevant screens', () => {
  assert.deepEqual(U.parseRoute('#/courses'), { page: 'courses' });
  assert.deepEqual(U.parseRoute('#/feedback'), { page: 'feedback' });
  assert.deepEqual(U.parseRoute('#/course/cs3141'), { page: 'course', courseId: 'cs3141' });
  assert.deepEqual(U.parseRoute('#/course/cs3141/grades'), { page: 'grades', courseId: 'cs3141' });
  assert.deepEqual(U.parseRoute('#/course/cs3141/assignment/proposal'), { page: 'assignment', courseId: 'cs3141', assignmentId: 'proposal' });
  assert.deepEqual(U.parseRoute('#/garbage'), { page: 'welcome' });
});

test('HTML escaping prevents demo data from injecting markup', () => {
  assert.equal(U.escapeHtml('<img src="x" onerror="alert(1)">'), '&lt;img src=&quot;x&quot; onerror=&quot;alert(1)&quot;&gt;');
});
