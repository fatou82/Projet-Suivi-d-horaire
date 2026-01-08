import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EmployeDashComponent } from './employe-dash.component';

describe('EmployeDashComponent', () => {
  let component: EmployeDashComponent;
  let fixture: ComponentFixture<EmployeDashComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EmployeDashComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EmployeDashComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
