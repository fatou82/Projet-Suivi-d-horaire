import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EmployePointageComponent } from './employe-pointage.component';

describe('EmployePointageComponent', () => {
  let component: EmployePointageComponent;
  let fixture: ComponentFixture<EmployePointageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EmployePointageComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EmployePointageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
