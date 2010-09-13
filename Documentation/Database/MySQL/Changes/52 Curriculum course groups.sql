/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2009, UniTime LLC
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/

alter table curriculum_group add curriculum_id decimal(20,0) not null;

alter table curriculum_group add constraint fk_curriculum_group_curriculum foreign key (curriculum_id)
	references curriculum (uniqueid) on delete cascade;


/**
 * Update database version
 */

update application_config set value='52' where name='tmtbl.db.version';

commit;