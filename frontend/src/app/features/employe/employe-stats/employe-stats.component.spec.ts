import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EmployeStatsComponent } from './employe-stats.component';

describe('EmployeStatsComponent', () => {
  let component: EmployeStatsComponent;
  let fixture: ComponentFixture<EmployeStatsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EmployeStatsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EmployeStatsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
