; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at http://mozilla.org/MPL/2.0/.

; Tested with Z3 4.8.7 and 4.12.1

; ATTENTION: Continuing multi-line statements must be indented with at least
;            one tab or two spaces. All other lines must not start with tabs
;            or more than one space.

; Currently, print-success MUST come first, because it guarantees that every query to Z3, including
; setting options, is answered by a success (or error) reply from Z3. Silicon currently relies on
; these replies when it interacts with Z3 via stdio.
(set-option :print-success true) ; Boogie: false
(set-option :global-decls true) ; Necessary for push pop mode

(set-option :smt.mbqi false)
; Since auto-config might re-enable mbqi we need to make sure the unsound SortWrapper quantifiers are not seen by mbqi
(set-option :smt.mbqi.id "THIS_NEVER_APPEARS")

; We want unlimited multipatterns
(set-option :smt.qi.max_multi_patterns 1000)
