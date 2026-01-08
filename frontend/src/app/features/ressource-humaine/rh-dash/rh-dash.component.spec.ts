import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RhDashComponent } from './rh-dash.component';

describe('RhDashComponent', () => {
  let component: RhDashComponent;
  let fixture: ComponentFixture<RhDashComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RhDashComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RhDashComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
